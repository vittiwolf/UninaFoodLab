# 08 - Testing e Debugging

## Indice
- [Strategia di Testing](#strategia-di-testing)
- [Unit Testing](#unit-testing)
- [Integration Testing](#integration-testing)
- [UI Testing](#ui-testing)
- [Performance Testing](#performance-testing)
- [Debugging](#debugging)
- [Test Data Management](#test-data-management)
- [Continuous Testing](#continuous-testing)

## Strategia di Testing

### Piramide dei Test

```
         /\
        /  \
       / UI \           E2E Tests (5%)
      /______\          - Flussi utente completi
     /        \         - Test interfaccia grafica
    /Integration\       Integration Tests (20%)
   /__Tests____\        - Test integrazione database
  /              \      - Test integrazione servizi
 /   Unit Tests   \     Unit Tests (75%)
/_______________\       - Test logica business
                        - Test validazioni
                        - Test utility
```

### Classificazione Test

| Tipo | Percentuale | Caratteristiche | Strumenti |
|------|-------------|-----------------|-----------|
| **Unit** | 75% | Veloci, isolati, specifici | JUnit 5, Mockito |
| **Integration** | 20% | Database, servizi esterni | TestContainers |
| **E2E/UI** | 5% | Flussi completi utente | TestFX |

## Unit Testing

### Setup Framework

#### Dipendenze Maven

```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.9.1</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>4.8.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito JUnit Jupiter -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <version>4.8.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.23.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Test Service Layer

#### UninaFoodLabServiceTest

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("UninaFoodLabService Tests")
class UninaFoodLabServiceTest {
    
    @Mock
    private ChefDAO chefDAO;
    
    @Mock
    private CorsoDAO corsoDAO;
    
    @Mock
    private SessioneDAO sessioneDAO;
    
    @InjectMocks
    private UninaFoodLabService service;
    
    @Nested
    @DisplayName("Autenticazione Chef")
    class AutenticazioneTest {
        
        @Test
        @DisplayName("Dovrebbe autenticare chef con credenziali corrette")
        void dovrebbeAutenticareChefConCredenziali() {
            // Given
            String username = "chef.mario";
            String password = "password123";
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            
            Chef expectedChef = new Chef();
            expectedChef.setId(1L);
            expectedChef.setUsername(username);
            expectedChef.setPassword(hashedPassword);
            
            when(chefDAO.findByUsername(username)).thenReturn(expectedChef);
            
            // When
            Chef result = service.autenticaChef(username, password);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo(username);
            
            verify(chefDAO).findByUsername(username);
            verify(chefDAO).updateUltimoAccesso(1L);
        }
        
        @Test
        @DisplayName("Dovrebbe fallire con password errata")
        void dovrebbeRifiutarePasswordErrata() {
            // Given
            String username = "chef.mario";
            String password = "passwordErrata";
            String hashedPassword = BCrypt.hashpw("passwordCorretta", BCrypt.gensalt());
            
            Chef chef = new Chef();
            chef.setUsername(username);
            chef.setPassword(hashedPassword);
            
            when(chefDAO.findByUsername(username)).thenReturn(chef);
            
            // When & Then
            assertThatThrownBy(() -> service.autenticaChef(username, password))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Credenziali non valide");
            
            verify(chefDAO).findByUsername(username);
            verify(chefDAO, never()).updateUltimoAccesso(any());
        }
        
        @ParameterizedTest
        @DisplayName("Dovrebbe rifiutare input non validi")
        @CsvSource({
            "'', password123, 'Username obbligatorio'",
            "username, '', 'Password obbligatoria'",
            "null, password123, 'Username obbligatorio'",
            "username, null, 'Password obbligatoria'"
        })
        void dovrebbeRifiutareInputNonValidi(String username, String password, String expectedMessage) {
            // When & Then
            assertThatThrownBy(() -> service.autenticaChef(username, password))
                .isInstanceOf(ValidationException.class)
                .hasMessage(expectedMessage);
        }
    }
    
    @Nested
    @DisplayName("Gestione Corsi")
    class GestioneCorsiTest {
        
        @Test
        @DisplayName("Dovrebbe creare corso valido")
        void dovrebbereareCorsoValido() {
            // Given
            Corso corso = new Corso();
            corso.setNome("Cucina Italiana Base");
            corso.setDescrizione("Corso introduttivo alla cucina italiana");
            corso.setCategoria(CategoriaCorso.CUCINA_REGIONALE);
            corso.setChefId(1L);
            
            Corso savedCorso = new Corso();
            savedCorso.setId(1L);
            savedCorso.setNome(corso.getNome());
            savedCorso.setDescrizione(corso.getDescrizione());
            savedCorso.setCategoria(corso.getCategoria());
            savedCorso.setChefId(corso.getChefId());
            
            when(corsoDAO.save(corso)).thenReturn(savedCorso);
            
            // When
            Corso result = service.creaCorso(corso);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getNome()).isEqualTo("Cucina Italiana Base");
            
            verify(corsoDAO).save(corso);
        }
        
        @Test
        @DisplayName("Dovrebbe validare dati corso")
        void dovrebbeValidareDatiCorso() {
            // Given
            Corso corso = new Corso();
            corso.setNome(""); // Nome vuoto - non valido
            corso.setChefId(1L);
            
            // When & Then
            assertThatThrownBy(() -> service.creaCorso(corso))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Nome corso obbligatorio");
            
            verify(corsoDAO, never()).save(any());
        }
        
        @Test
        @DisplayName("Dovrebbe trovare corsi per chef")
        void dovrebbeTrovareCorsiPerChef() {
            // Given
            Long chefId = 1L;
            List<Corso> expectedCorsi = Arrays.asList(
                createCorso(1L, "Corso 1"),
                createCorso(2L, "Corso 2")
            );
            
            when(corsoDAO.findByChefId(chefId)).thenReturn(expectedCorsi);
            
            // When
            List<Corso> result = service.getCorsiByChef(chefId);
            
            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getNome()).isEqualTo("Corso 1");
            assertThat(result.get(1).getNome()).isEqualTo("Corso 2");
            
            verify(corsoDAO).findByChefId(chefId);
        }
    }
    
    @Nested
    @DisplayName("Gestione Sessioni")
    class GestioneSessioniTest {
        
        @Test
        @DisplayName("Dovrebbe aggiungere sessione a corso")
        void dovrebbeAggiungereSessioneACorso() {
            // Given
            Long corso_id = 1L;
            Sessione sessione = new Sessione();
            sessione.setCorsoId(corso_id);
            sessione.setDataOra(LocalDateTime.now().plusDays(1));
            sessione.setModalita(ModalitaSessione.PRESENZA);
            sessione.setMaxPartecipanti(20);
            
            Corso corso = createCorso(corso_id, "Test Corso");
            
            when(corsoDAO.findById(corso_id)).thenReturn(corso);
            when(sessioneDAO.existsByCorsoAndData(corso_id, sessione.getDataOra())).thenReturn(false);
            when(sessioneDAO.save(sessione)).thenReturn(sessione);
            
            // When
            Sessione result = service.aggiungiSessione(sessione);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCorsoId()).isEqualTo(corso_id);
            
            verify(corsoDAO).findById(corso_id);
            verify(sessioneDAO).existsByCorsoAndData(corso_id, sessione.getDataOra());
            verify(sessioneDAO).save(sessione);
        }
        
        @Test
        @DisplayName("Dovrebbe rifiutare sessione con data nel passato")
        void dovrebbeRifiutareSessioneConDataPassato() {
            // Given
            Sessione sessione = new Sessione();
            sessione.setDataOra(LocalDateTime.now().minusDays(1)); // Data nel passato
            
            // When & Then
            assertThatThrownBy(() -> service.aggiungiSessione(sessione))
                .isInstanceOf(ValidationException.class)
                .hasMessage("La data della sessione non può essere nel passato");
        }
    }
    
    // Utility methods
    private Corso createCorso(Long id, String nome) {
        Corso corso = new Corso();
        corso.setId(id);
        corso.setNome(nome);
        corso.setCategoria(CategoriaCorso.CUCINA_REGIONALE);
        corso.setChefId(1L);
        return corso;
    }
}
```

### Test DAO Layer

#### ChefDAOTest

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ChefDAO Tests")
class ChefDAOTest {
    
    private static final String TEST_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    
    private ChefDAO chefDAO;
    private Connection connection;
    
    @BeforeAll
    void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection(TEST_DB_URL, "sa", "");
        chefDAO = new ChefDAO(connection);
        
        // Creazione schema test
        createTestSchema();
        insertTestData();
    }
    
    @AfterAll
    void cleanupDatabase() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    @BeforeEach
    void setupTestCase() {
        // Reset dati per ogni test se necessario
    }
    
    @Test
    @DisplayName("Dovrebbe salvare nuovo chef")
    void dovrebbeSalvareNuovoChef() throws ServiceException {
        // Given
        Chef chef = new Chef();
        chef.setNome("Mario");
        chef.setCognome("Rossi");
        chef.setUsername("mario.rossi");
        chef.setEmail("mario.rossi@test.com");
        chef.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
        chef.setSpecializzazione("Cucina Italiana");
        
        // When
        Chef savedChef = chefDAO.save(chef);
        
        // Then
        assertThat(savedChef).isNotNull();
        assertThat(savedChef.getId()).isNotNull();
        assertThat(savedChef.getNome()).isEqualTo("Mario");
        assertThat(savedChef.getUsername()).isEqualTo("mario.rossi");
        assertThat(savedChef.getCreatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("Dovrebbe trovare chef per username")
    void dovrebbeTrovareChefPerUsername() throws ServiceException {
        // When
        Chef chef = chefDAO.findByUsername("chef.luigi");
        
        // Then
        assertThat(chef).isNotNull();
        assertThat(chef.getNome()).isEqualTo("Luigi");
        assertThat(chef.getCognome()).isEqualTo("Verdi");
    }
    
    @Test
    @DisplayName("Dovrebbe restituire null per username inesistente")
    void dovrebbeRestituireNullPerUsernameInesistente() throws ServiceException {
        // When
        Chef chef = chefDAO.findByUsername("username.inesistente");
        
        // Then
        assertThat(chef).isNull();
    }
    
    @Test
    @DisplayName("Dovrebbe aggiornare ultimo accesso")
    void dovrebbeAggiornareUltimoAccesso() throws ServiceException {
        // Given
        Long chefId = 1L;
        LocalDateTime before = LocalDateTime.now().minusMinutes(1);
        
        // When
        chefDAO.updateUltimoAccesso(chefId);
        
        // Then
        Chef chef = chefDAO.findById(chefId);
        assertThat(chef.getUltimoAccesso()).isAfter(before);
    }
    
    @Test
    @DisplayName("Dovrebbe lanciare eccezione per username duplicato")
    void dovrebbeLanciareEccezionePerUsernameDuplicato() {
        // Given
        Chef chef1 = createTestChef("user.test", "test1@email.com");
        Chef chef2 = createTestChef("user.test", "test2@email.com"); // Username duplicato
        
        // When & Then
        assertDoesNotThrow(() -> chefDAO.save(chef1));
        
        assertThatThrownBy(() -> chefDAO.save(chef2))
            .isInstanceOf(ServiceException.class)
            .hasMessageContaining("Username già esistente");
    }
    
    private void createTestSchema() throws SQLException {
        String createChefTable = """
            CREATE TABLE IF NOT EXISTS chef (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                nome VARCHAR(50) NOT NULL,
                cognome VARCHAR(50) NOT NULL,
                username VARCHAR(30) UNIQUE NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                specializzazione VARCHAR(100),
                ultimo_accesso TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(createChefTable)) {
            stmt.executeUpdate();
        }
    }
    
    private void insertTestData() throws SQLException {
        String insertChef = """
            INSERT INTO chef (nome, cognome, username, email, password, specializzazione)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(insertChef)) {
            stmt.setString(1, "Luigi");
            stmt.setString(2, "Verdi");
            stmt.setString(3, "chef.luigi");
            stmt.setString(4, "luigi.verdi@test.com");
            stmt.setString(5, BCrypt.hashpw("password123", BCrypt.gensalt()));
            stmt.setString(6, "Cucina Francese");
            stmt.executeUpdate();
        }
    }
    
    private Chef createTestChef(String username, String email) {
        Chef chef = new Chef();
        chef.setNome("Test");
        chef.setCognome("User");
        chef.setUsername(username);
        chef.setEmail(email);
        chef.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        return chef;
    }
}
```

## Integration Testing

### Database Integration Tests

#### Setup TestContainers

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.17.6</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.17.6</version>
    <scope>test</scope>
</dependency>
```

#### Database Integration Test

```java
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Database Integration Tests")
class DatabaseIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("test_uninafoodlab")
            .withUsername("test_user")
            .withPassword("test_password")
            .withInitScript("test-schema.sql");
    
    private UninaFoodLabService service;
    private Connection connection;
    
    @BeforeAll
    void setup() throws SQLException {
        // Configurazione connessione database test
        String jdbcUrl = postgres.getJdbcUrl();
        connection = DriverManager.getConnection(
            jdbcUrl, 
            postgres.getUsername(), 
            postgres.getPassword()
        );
        
        // Inizializzazione servizi
        ChefDAO chefDAO = new ChefDAO(connection);
        CorsoDAO corsoDAO = new CorsoDAO(connection);
        SessioneDAO sessioneDAO = new SessioneDAO(connection);
        
        service = new UninaFoodLabService(chefDAO, corsoDAO, sessioneDAO);
    }
    
    @Test
    @DisplayName("Dovrebbe eseguire flusso completo creazione corso")
    @Transactional
    void dovrebbeEseguireFlussoCompletoCreazionCorso() throws ServiceException {
        // Given - Creare chef
        Chef chef = new Chef();
        chef.setNome("Integration");
        chef.setCognome("Test");
        chef.setUsername("integration.test");
        chef.setEmail("integration@test.com");
        chef.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        
        Chef savedChef = service.creaChef(chef);
        
        // When - Creare corso
        Corso corso = new Corso();
        corso.setNome("Corso Integration Test");
        corso.setDescrizione("Corso per test di integrazione");
        corso.setCategoria(CategoriaCorso.CUCINA_REGIONALE);
        corso.setChefId(savedChef.getId());
        
        Corso savedCorso = service.creaCorso(corso);
        
        // Then - Verificare persistenza
        assertThat(savedCorso.getId()).isNotNull();
        
        List<Corso> corsiChef = service.getCorsiByChef(savedChef.getId());
        assertThat(corsiChef).hasSize(1);
        assertThat(corsiChef.get(0).getNome()).isEqualTo("Corso Integration Test");
        
        // When - Aggiungere sessione
        Sessione sessione = new Sessione();
        sessione.setCorsoId(savedCorso.getId());
        sessione.setDataOra(LocalDateTime.now().plusDays(1));
        sessione.setModalita(ModalitaSessione.PRESENZA);
        sessione.setMaxPartecipanti(25);
        
        Sessione savedSessione = service.aggiungiSessione(sessione);
        
        // Then - Verificare sessione
        assertThat(savedSessione.getId()).isNotNull();
        
        List<Sessione> sessioni = service.getSessioniByCorso(savedCorso.getId());
        assertThat(sessioni).hasSize(1);
    }
    
    @Test
    @DisplayName("Dovrebbe gestire transazioni con rollback")
    void dovrebbeGestireTransazioniConRollback() {
        // Test che verifica il corretto funzionamento delle transazioni
        // quando si verifica un errore durante operazioni multiple
        
        assertThatThrownBy(() -> {
            // Tentativo di operazione che dovrebbe fallire
            Chef chef = new Chef();
            chef.setNome("Test");
            // Mancano campi obbligatori - dovrebbe fallire
            service.creaChef(chef);
        }).isInstanceOf(ValidationException.class);
        
        // Verificare che non ci siano dati sporchi nel database
        List<Chef> allChef = service.getAllChef();
        assertThat(allChef).noneMatch(c -> "Test".equals(c.getNome()));
    }
}
```

## UI Testing

### Setup TestFX

```xml
<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-junit5</artifactId>
    <version>4.0.16-alpha</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testfx</groupId>
    <artifactId>testfx-core</artifactId>
    <version>4.0.16-alpha</version>
    <scope>test</scope>
</dependency>
```

### UI Integration Test

```java
@ExtendWith(ApplicationExtension.class)
@DisplayName("UI Integration Tests")
class UIIntegrationTest extends ApplicationTest {
    
    private UninaFoodLabService mockService;
    
    @Override
    public void start(Stage stage) {
        // Setup mock service
        mockService = Mockito.mock(UninaFoodLabService.class);
        
        // Configurare applicazione per test
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            LoginController controller = new LoginController();
            controller.setService(mockService); // Iniettare mock
            loader.setController(controller);
            
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    @DisplayName("Dovrebbe permettere login con credenziali corrette")
    void dovrebbePermettereLoginConCredenzialiCorrette(FxRobot robot) {
        // Given
        Chef testChef = new Chef();
        testChef.setId(1L);
        testChef.setNome("Test");
        testChef.setUsername("test.user");
        
        when(mockService.autenticaChef("test.user", "password123"))
            .thenReturn(testChef);
        
        // When
        robot.clickOn("#txtUsername");
        robot.write("test.user");
        robot.clickOn("#txtPassword");
        robot.write("password123");
        robot.clickOn("#btnLogin");
        
        // Then
        // Verificare che la finestra principale si apra
        verifyThat("#mainWindow", NodeMatchers.isVisible());
    }
    
    @Test
    @DisplayName("Dovrebbe mostrare errore per credenziali errate")
    void dovrebbeMostrareErrorePerCredenzialiErrate(FxRobot robot) {
        // Given
        when(mockService.autenticaChef("wrong.user", "wrongpassword"))
            .thenThrow(new AuthenticationException("Credenziali non valide"));
        
        // When
        robot.clickOn("#txtUsername");
        robot.write("wrong.user");
        robot.clickOn("#txtPassword");
        robot.write("wrongpassword");
        robot.clickOn("#btnLogin");
        
        // Then
        verifyThat("#lblErrore", NodeMatchers.isVisible());
        verifyThat("#lblErrore", LabeledMatchers.hasText("Credenziali non valide"));
    }
    
    @Test
    @DisplayName("Dovrebbe validare campi vuoti")
    void dovrebbeValidareCampiVuoti(FxRobot robot) {
        // When - Tentare login con campi vuoti
        robot.clickOn("#btnLogin");
        
        // Then
        verifyThat("#btnLogin", Node::isDisabled);
        
        // When - Compilare solo username
        robot.clickOn("#txtUsername");
        robot.write("test");
        
        // Then
        verifyThat("#btnLogin", Node::isDisabled);
        
        // When - Compilare anche password
        robot.clickOn("#txtPassword");
        robot.write("password");
        
        // Then
        verifyThat("#btnLogin", not(Node::isDisabled));
    }
}
```

## Performance Testing

### Benchmark con JMH

```xml
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>1.36</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-generator-annprocess</artifactId>
    <version>1.36</version>
    <scope>test</scope>
</dependency>
```

### Performance Test

```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class ServicePerformanceTest {
    
    private UninaFoodLabService service;
    private Chef testChef;
    
    @Setup
    public void setup() {
        // Configurazione service con database in-memory
        service = createTestService();
        testChef = createTestChef();
    }
    
    @Benchmark
    public List<Corso> benchmarkGetCorsiByChef() {
        return service.getCorsiByChef(testChef.getId());
    }
    
    @Benchmark
    public Chef benchmarkAutenticaChef() {
        return service.autenticaChef("test.user", "password123");
    }
    
    @Benchmark
    public Corso benchmarkCreaCorso() {
        Corso corso = new Corso();
        corso.setNome("Benchmark Corso " + System.nanoTime());
        corso.setCategoria(CategoriaCorso.CUCINA_REGIONALE);
        corso.setChefId(testChef.getId());
        return service.creaCorso(corso);
    }
    
    // Utility methods...
}
```

### Load Testing

```java
@Test
@DisplayName("Load Test - Creazione multipli corsi concorrenti")
void loadTestCreazioneCorsiConcorrenti() throws InterruptedException {
    int numberOfThreads = 10;
    int coursesPerThread = 100;
    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    
    // Misurare tempo di esecuzione
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < numberOfThreads; i++) {
        final int threadId = i;
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                for (int j = 0; j < coursesPerThread; j++) {
                    Corso corso = new Corso();
                    corso.setNome("Corso Thread-" + threadId + " Num-" + j);
                    corso.setCategoria(CategoriaCorso.CUCINA_REGIONALE);
                    corso.setChefId(1L);
                    
                    service.creaCorso(corso);
                }
            } catch (Exception e) {
                logger.error("Errore durante load test", e);
            } finally {
                latch.countDown();
            }
        }, executor);
        
        futures.add(future);
    }
    
    // Attendere completamento
    latch.await(30, TimeUnit.SECONDS);
    long endTime = System.currentTimeMillis();
    
    // Verificare risultati
    long totalCourses = numberOfThreads * coursesPerThread;
    long executionTime = endTime - startTime;
    double coursesPerSecond = (double) totalCourses / (executionTime / 1000.0);
    
    logger.info("Load Test Results:");
    logger.info("- Total courses created: {}", totalCourses);
    logger.info("- Execution time: {} ms", executionTime);
    logger.info("- Courses per second: {:.2f}", coursesPerSecond);
    
    // Asserzioni performance
    assertThat(coursesPerSecond).isGreaterThan(10.0); // Min 10 corsi/secondo
    assertThat(executionTime).isLessThan(30000); // Max 30 secondi
    
    executor.shutdown();
}
```

## Debugging

### Logging per Debug

```java
// Configurazione logging dettagliato per debug
@Slf4j
public class UninaFoodLabService {
    
    public Corso creaCorso(Corso corso) {
        logger.debug("Inizio creazione corso: {}", corso.getNome());
        
        try {
            // Validazione
            ValidationResult validation = validateCorso(corso);
            logger.debug("Risultato validazione: {}", validation);
            
            if (!validation.isValid()) {
                logger.warn("Validazione fallita per corso: {}, errori: {}", 
                    corso.getNome(), validation.getErrors());
                throw new ValidationException(validation.getErrorMessage());
            }
            
            // Salvataggio
            Corso savedCorso = corsoDAO.save(corso);
            logger.debug("Corso salvato con ID: {}", savedCorso.getId());
            
            // Audit
            auditService.logAction("CORSO_CREATED", savedCorso.getId(), 
                "Creato corso: " + savedCorso.getNome());
            
            logger.info("Corso creato con successo: {} (ID: {})", 
                savedCorso.getNome(), savedCorso.getId());
            
            return savedCorso;
            
        } catch (Exception e) {
            logger.error("Errore durante creazione corso: {}", corso.getNome(), e);
            throw new ServiceException("Errore nella creazione del corso", e);
        }
    }
}
```

### Debug Utilities

```java
@Component
public class DebugUtils {
    
    /**
     * Dump dello stato dell'applicazione per debugging
     */
    public void dumpApplicationState() {
        logger.debug("=== APPLICATION STATE DUMP ===");
        
        // Database connection status
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            logger.debug("Database: {} {}", 
                metaData.getDatabaseProductName(), 
                metaData.getDatabaseProductVersion());
            logger.debug("Connection valid: {}", conn.isValid(5));
        } catch (SQLException e) {
            logger.error("Errore controllo database", e);
        }
        
        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        logger.debug("Memory - Total: {} MB, Used: {} MB, Free: {} MB",
            totalMemory / 1024 / 1024,
            usedMemory / 1024 / 1024,
            freeMemory / 1024 / 1024);
        
        // Thread count
        logger.debug("Active threads: {}", Thread.activeCount());
        
        logger.debug("=== END STATE DUMP ===");
    }
    
    /**
     * Profiling di un'operazione
     */
    public <T> T profileOperation(String operationName, Supplier<T> operation) {
        long startTime = System.nanoTime();
        logger.debug("Starting operation: {}", operationName);
        
        try {
            T result = operation.get();
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            
            logger.debug("Operation '{}' completed in {} ms", operationName, durationMs);
            
            if (durationMs > 1000) { // Log warning se > 1 secondo
                logger.warn("Slow operation detected: '{}' took {} ms", operationName, durationMs);
            }
            
            return result;
        } catch (Exception e) {
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            logger.error("Operation '{}' failed after {} ms", operationName, durationMs, e);
            throw e;
        }
    }
}
```

## Test Data Management

### Test Data Builder

```java
public class TestDataBuilder {
    
    public static class ChefBuilder {
        private Chef chef = new Chef();
        
        public ChefBuilder withNome(String nome) {
            chef.setNome(nome);
            return this;
        }
        
        public ChefBuilder withCognome(String cognome) {
            chef.setCognome(cognome);
            return this;
        }
        
        public ChefBuilder withUsername(String username) {
            chef.setUsername(username);
            return this;
        }
        
        public ChefBuilder withEmail(String email) {
            chef.setEmail(email);
            return this;
        }
        
        public ChefBuilder withPassword(String password) {
            chef.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            return this;
        }
        
        public ChefBuilder withDefaults() {
            return withNome("Test")
                .withCognome("Chef")
                .withUsername("test.chef." + System.currentTimeMillis())
                .withEmail("test" + System.currentTimeMillis() + "@test.com")
                .withPassword("password123");
        }
        
        public Chef build() {
            return chef;
        }
    }
    
    public static class CorsoBuilder {
        private Corso corso = new Corso();
        
        public CorsoBuilder withNome(String nome) {
            corso.setNome(nome);
            return this;
        }
        
        public CorsoBuilder withDescrizione(String descrizione) {
            corso.setDescrizione(descrizione);
            return this;
        }
        
        public CorsoBuilder withCategoria(CategoriaCorso categoria) {
            corso.setCategoria(categoria);
            return this;
        }
        
        public CorsoBuilder withChefId(Long chefId) {
            corso.setChefId(chefId);
            return this;
        }
        
        public CorsoBuilder withDefaults() {
            return withNome("Corso Test " + System.currentTimeMillis())
                .withDescrizione("Descrizione corso di test")
                .withCategoria(CategoriaCorso.CUCINA_REGIONALE)
                .withChefId(1L);
        }
        
        public Corso build() {
            return corso;
        }
    }
    
    // Factory methods
    public static ChefBuilder chef() {
        return new ChefBuilder();
    }
    
    public static CorsoBuilder corso() {
        return new CorsoBuilder();
    }
}

// Utilizzo nei test
@Test
void testExample() {
    Chef chef = chef().withDefaults().withNome("Mario").build();
    Corso corso = corso().withDefaults().withChefId(chef.getId()).build();
    
    // Test logic...
}
```

## Continuous Testing

### Maven Surefire Configuration

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M8</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
        </includes>
        <groups>unit</groups>
        <systemProperties>
            <property>
                <name>java.awt.headless</name>
                <value>true</value>
            </property>
        </systemProperties>
    </configuration>
</plugin>

<!-- Plugin per integration tests -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>3.0.0-M8</version>
    <configuration>
        <groups>integration</groups>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Comandi Maven per Testing

```bash
# Eseguire solo unit test
mvn test

# Eseguire solo integration test
mvn verify -DskipUnitTests=true

# Eseguire tutti i test
mvn verify

# Eseguire test con coverage
mvn clean verify jacoco:report

# Eseguire test specifici
mvn test -Dtest=UninaFoodLabServiceTest

# Eseguire test con profiling
mvn test -Djvm.args="-XX:+FlightRecorder -XX:StartFlightRecording=duration=60s,filename=test-profile.jfr"
```

### CI/CD Pipeline

```yaml
# GitHub Actions esempio
name: Test Suite

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:14
        env:
          POSTGRES_PASSWORD: test_password
          POSTGRES_DB: test_uninafoodlab
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run unit tests
      run: mvn test
    
    - name: Run integration tests
      run: mvn verify -DskipUnitTests=true
      env:
        DB_HOST: localhost
        DB_PORT: 5432
    
    - name: Generate test report
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
```

---

*Questa strategia di testing completa garantisce la qualità e l'affidabilità del sistema UninaFoodLab attraverso test automatizzati a tutti i livelli dell'architettura.*
