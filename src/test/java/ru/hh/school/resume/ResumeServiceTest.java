package ru.hh.school.resume;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import ru.hh.school.users.resume.Resume;
import ru.hh.school.users.resume.ResumeDao;
import ru.hh.school.users.resume.ResumeService;
import ru.hh.school.users.user.User;

public class ResumeServiceTest {

    private static ResumeService resumeService;
    private static ResumeDao resumeDao;
    private static SessionFactory sessionFactory;
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test")
        .waitingFor(Wait.forListeningPort());

    @BeforeAll
    public static void setUp() {
        postgreSQLContainer.start();
        sessionFactory = createSessionFactory();
        resumeDao = new ResumeDao(sessionFactory);

        resumeService = new ResumeService(
            sessionFactory,
            resumeDao
        );
    }

    @AfterAll
    public static void shutdown() {
        postgreSQLContainer.close();
    }


    @BeforeEach
    public void cleanUpDb() {
        resumeService.deleteAll();
    }


    private static SessionFactory createSessionFactory() {
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
            .loadProperties("hibernate.properties")
            .applySetting("hibernate.connection.url", postgreSQLContainer.getJdbcUrl())
            .applySetting("hibernate.connection.username", postgreSQLContainer.getUsername())
            .applySetting("hibernate.connection.password", postgreSQLContainer.getPassword())
            .build();

        Metadata metadata = new MetadataSources(serviceRegistry)
            .addAnnotatedClass(Resume.class)
            .addAnnotatedClass(User.class)
            .buildMetadata();

        return metadata.buildSessionFactory();
    }


    // добавить сохранение резюмешек по 1 с юзером
    // потом достаем юзера и у него есть все резюме

    // достаем резюме и с фетч тайп eager у него есть юзер в дебаге
    // достаем резюме с лейзи в дебаге у него прокся

    // тест на лейзи инит ех
    // тест на джоин фетч
    // тест на дозапрос, тк в одной транзакции всё

    // тест достаю юзера и с ним все резюмехи идут пачкой тк джоин фетч

    @Test
    void saveNewResumeShouldInsertDbRow() {
//        Resume resume = new Resume();
//        resume.setUserId(1);
//        resume.setDescription("description");
//        resumeService.saveNew(resume);
//
//        Optional<Resume> result = resumeService.getBy(resume.getId());
//        assertTrue(result.isPresent());
//        assertEquals(resume, result.get());
    }

    @Test
    public void shouldGetOnlyActiveResumesForUser() {
        Resume resume1 = new Resume();
//        resume1.setUserId(1);
        resume1.setActive(true);
        resume1.setDescription("its me!");
        resumeService.saveNew(resume1);

        Resume resume2 = new Resume();
//        resume2.setUserId(1);
        resume2.setActive(false);
        resume2.setDescription("im not active");
        resumeService.saveNew(resume2);

        Resume resume3 = new Resume();
//        resume3.setUserId(2);
        resume2.setActive(true);
        resume1.setDescription("im for another user");
        resumeService.saveNew(resume3);

//        Set<Resume> activeResumesForUserId = resumeService.getActiveResumesForUserId(1);

//        assertEquals(1, activeResumesForUserId.size());
//        assertTrue(activeResumesForUserId.stream().anyMatch(r -> r.getDescription().equals("its me!")));
    }

}
