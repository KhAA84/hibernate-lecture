package ru.hh.school.relations;

import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import ru.hh.school.TestHelper;
import ru.hh.school.users.TransactionHelper;
import ru.hh.school.users.resume.Resume;
import ru.hh.school.users.resume.ResumeDao;
import ru.hh.school.users.resume.ResumeService;
import ru.hh.school.users.user.User;
import ru.hh.school.users.user.UserDao;
import ru.hh.school.users.user.UserService;

import java.util.Set;

public class RelationsTest {

  private static ResumeDao resumeDao;
  private static UserDao userDao;
  private static UserService userService;
  private static ResumeService resumeService;
  private static SessionFactory sessionFactory;
  private static TransactionHelper th;

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
    userDao = new UserDao(sessionFactory);

    resumeService = new ResumeService(sessionFactory, resumeDao);
    userService = new UserService(sessionFactory, userDao);
    th = new TransactionHelper(sessionFactory);
  }

  @AfterAll
  public static void shutdown() {
    postgreSQLContainer.close();
  }

  @BeforeEach
  public void cleanUpDb() {
    userService.deleteAll();
    resumeService.deleteAll();
  }

  public void insert_users() {
    TestHelper.executeScript(sessionFactory, "insert_hhusers.sql");
  }

  public void insert_resumes() {
    TestHelper.executeScript(sessionFactory, "insert_resumes.sql");
  }

  @Test
  void saveResumeForUser() {
    insert_users();

    User user = userService.getAll().stream().findFirst().get();

    Resume resume1 = createResume(user, "Java Dev", true);
    Resume resume2 = createResume(user, "Python Dev", true);

    resumeService.saveNew(resume1);
    resumeService.saveNew(resume2);

    // todo get user, fetch resumes
//    User userWithResumes = th.inTransaction(() -> userDao.getUserByIdWithResumes(user.getId()));
//
//    assertEquals(2, userWithResumes.getResumes().size());
//    assertTrue(
//        userWithResumes.getResumes().stream()
//            .anyMatch(resume -> resume.getDescription().equals("Java Dev"))
//    );
//
//    assertTrue(
//        userWithResumes.getResumes().stream()
//            .anyMatch(resume -> resume.getDescription().equals("Python Dev"))
//    );
  }

  @Test
  void getResumeWithDifferentFetchType() {
    insert_users();
    insert_resumes();

    Resume resumeFromDb = resumeService.getBy(1).get();
    assertThrows(LazyInitializationException.class, () -> resumeFromDb.getUser().getFirstName());
    assertEquals(1, resumeFromDb.getUser().getId());
  }

  @Test
  void getResumeFetchUser() {
    insert_users();
    insert_resumes();

    Resume resumeWithUser = resumeService.getResumeWithUserById(1);
    assertFalse(resumeWithUser.getUser().getLastName().isEmpty());
  }

  @Test
  void getResumeWithoutUserFetchLazy() {
    insert_users();
    insert_resumes();

    th.inTransaction(() -> {
      Resume resume = resumeDao.getBy(1).get();

      assertFalse(resume.getUser().getLastName().isEmpty());
    });
  }

  @Test
  public void getResumeNPlusOne() {
    insert_users();

    userService.getAll().forEach(user -> {
      Resume resume = createResume(user, "Java Dev", true);
      resumeService.saveNew(resume);
    });

    th.inTransaction(() -> {
      Set<Resume> resumes = resumeDao.getAll();

      resumes.forEach(
          resume -> assertFalse(resume.getUser().getLastName().isEmpty())
      );
    });
  }

  private Resume createResume(User user, String description, boolean active) {
    Resume resume = new Resume();
    resume.setUser(user);
    resume.setDescription(description);
    resume.setActive(active);
    return resume;
  }

  private static SessionFactory createSessionFactory() {
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
        .loadProperties("hibernate.properties")
        .applySetting("hibernate.connection.url", postgreSQLContainer.getJdbcUrl())
        .applySetting("hibernate.connection.username", postgreSQLContainer.getUsername())
        .applySetting("hibernate.connection.password", postgreSQLContainer.getPassword())
        .build();

    Metadata metadata = new MetadataSources(serviceRegistry)
        .addAnnotatedClass(User.class)
        .addAnnotatedClass(Resume.class)
        .buildMetadata();

    return metadata.buildSessionFactory();
  }
}
