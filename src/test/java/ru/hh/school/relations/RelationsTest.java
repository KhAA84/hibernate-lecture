package ru.hh.school.relations;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.hh.school.TestHelper;
import ru.hh.school.users.TransactionHelper;
import ru.hh.school.users.resume.Resume;
import ru.hh.school.users.resume.ResumeDao;
import ru.hh.school.users.resume.ResumeService;
import ru.hh.school.users.user.User;
import ru.hh.school.users.user.UserDao;
import ru.hh.school.users.user.UserService;

import java.io.IOException;

import static org.junit.Assert.assertFalse;

public class RelationsTest {

  private static ResumeDao resumeDao;
  private static UserDao userDao;
  private static UserService userService;
  private static ResumeService resumeService;
  private static TransactionHelper th;

  private static EmbeddedPostgres embeddedPostgres = null;

  @BeforeClass
  public static void setUp() {
    try {
      embeddedPostgres = EmbeddedPostgres.builder()
          .setPort(5433)
          .start();
    } catch (IOException e) {
      e.printStackTrace();
    }

    SessionFactory sessionFactory = createSessionFactory();

    resumeDao = new ResumeDao(sessionFactory);
    userDao = new UserDao(sessionFactory);
    th = new TransactionHelper(sessionFactory);
    resumeService = new ResumeService(sessionFactory, resumeDao);
    userService = new UserService(sessionFactory, userDao);

    if (embeddedPostgres != null) {
      TestHelper.executeScript(embeddedPostgres.getPostgresDatabase(), "create_resume.sql");
      TestHelper.executeScript(embeddedPostgres.getPostgresDatabase(), "create_hhuser.sql");
    }
  }

  @AfterClass
  public static void shutdown() {
    try {
      embeddedPostgres.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Before
  public void cleanUpDb() {
    userService.deleteAll();
    resumeService.deleteAll();
  }

  public void insert_users() {
    TestHelper.executeScript(embeddedPostgres.getPostgresDatabase(), "insert_hhusers.sql");
  }

  public void insert_resumes() {
    TestHelper.executeScript(embeddedPostgres.getPostgresDatabase(), "insert_resumes.sql");
  }

  @Test
  public void saveResumeForUser() {
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

  @Test(expected = LazyInitializationException.class)
  public void getResumeWithDifferentFetchType() {
    insert_users();
    insert_resumes();

    Resume resumeFromDb = resumeService.getBy(1).get();
    resumeFromDb.getUser().getFirstName();
  }

  @Test
  public void getResumeFetchUser() {
    insert_users();
    insert_resumes();

    Resume resumeWithUser = resumeService.getResumeWithUserById(1);
    assertFalse(resumeWithUser.getUser().getLastName().isEmpty());
  }

  @Test
  public void getResumeWithoutUserFetchLazy() {
    insert_users();
    insert_resumes();

    th.inTransaction(() -> {
      Resume resume = resumeDao.getBy(1).get();

      assertFalse(resume.getUser().getLastName().isEmpty());
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
        .build();

    Metadata metadata = new MetadataSources(serviceRegistry)
        .addAnnotatedClass(Resume.class)
        .addAnnotatedClass(User.class)
        .buildMetadata();

    return metadata.buildSessionFactory();
  }
}
