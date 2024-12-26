package ru.hh.school.users;

import jakarta.persistence.EntityExistsException;
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
import ru.hh.school.users.resume.Resume;
import ru.hh.school.users.user.User;
import ru.hh.school.users.user.UserDao;
import ru.hh.school.users.user.UserService;

import java.util.Optional;
import java.util.Set;


public class UserServiceTest {

  private static UserService userService;
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

    userService = new UserService(
        sessionFactory,
        new UserDao(sessionFactory)
    );
  }

  @AfterAll
  public static void shutdown() {
    postgreSQLContainer.close();
  }

  private static SessionFactory createSessionFactory() {
    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
        .loadProperties("hibernate.properties")
        .applySetting("hibernate.connection.url", postgreSQLContainer.getJdbcUrl())
        .applySetting("hibernate.connection.username", postgreSQLContainer.getUsername())
        .applySetting("hibernate.connection.password", postgreSQLContainer.getPassword())
        .build();

    Metadata metadata = new MetadataSources(serviceRegistry)
        // add class
        .addAnnotatedClass(User.class)
        // добавляем Resume, потому что уже есть связь на них в User
        .addAnnotatedClass(Resume.class)
        .buildMetadata();

    return metadata.buildSessionFactory();
  }


  @BeforeEach
  public void cleanUpDb() {
    userService.deleteAll();
  }

  public void insert_users() {
    TestHelper.executeScript(sessionFactory, "insert_hhusers.sql");
  }

  @Test
  void getAllUsersShouldReturnAllUsers() {
    insert_users();
    final Set<User> all = userService.getAll();
    assertEquals(2, all.size());
    assertTrue(all.stream().anyMatch(u -> u.getFirstName().equals("Sarah")));
  }

  @Test
  void saveNewUserShouldInsertDbRow() {
    User user = new User("John", "Lennon");
    userService.saveNew(user);
    assertEquals(Set.of(user), userService.getAll());
  }

  @Test
  void savingOfExistingUserShouldBePrevented() {
    User user = new User("John", "Lennon");
    userService.saveNew(user);
    assertThrows(EntityExistsException.class, () -> userService.saveNew(user));
  }

  @Test
  void updateFirstNameShouldSucceed() {
    User user = new User("John", "Lennon");
    userService.saveNew(user);
    userService.changeFullName(user.getId(), "Paul", "McCartney");

    assertEquals(
        "Paul",
        userService.getBy(user.getId()).map(User::getFirstName).get()
    );
  }

  @Test
  void getByIdShouldReturnUserIfRowExists() {
    User user = new User("John", "Lennon");
    userService.saveNew(user);

    Optional<User> extractedUser = userService.getBy(user.getId());

    assertTrue(extractedUser.isPresent());
    assertEquals(user, extractedUser.get());
  }


  @Test
  void getByIdShouldReturnEmptyIfRowDoesntExist() {
    assertFalse(userService.getBy(-1).isPresent());
  }

  @Test
  void deleteUserShouldDeleteDbRow() {
    User user = new User("John", "Lennon");
    userService.saveNew(user);

    Optional<User> extractedUser = userService.getBy(user.getId());
    assertTrue(extractedUser.isPresent());

    userService.deleteBy(user.getId());

    extractedUser = userService.getBy(user.getId());
    assertFalse(extractedUser.isPresent());
  }

  @Test
  void updateShouldUpdateDbRowOfExistingUser() {
    User user = new User("Ringo", "Lennon");
    userService.saveNew(user);

    user.setFirstName("John");
    userService.update(user);

    assertEquals(
        "John",
        userService.getBy(user.getId()).map(User::getFirstName).orElse(null)
    );
  }

}
