package ru.hh.school.users;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.util.Optional;
import java.util.Set;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class UserDaoTest {

  private static UserDao userDao;
  private static PGSimpleDataSource ds;
  private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
      .withDatabaseName("test")
      .withUsername("test")
      .withPassword("test")
      .waitingFor(Wait.forListeningPort());



    @BeforeAll
    public static void setUpDataSource() {
      postgreSQLContainer.start();
      ds = new PGSimpleDataSource();
      ds.setServerNames(new String[]{postgreSQLContainer.getHost()});
      ds.setDatabaseName(postgreSQLContainer.getDatabaseName());
      ds.setUser(postgreSQLContainer.getUsername());
      ds.setPassword(postgreSQLContainer.getPassword());
      ds.setPortNumbers(new int[]{postgreSQLContainer.getMappedPort(5432)});
      userDao = new UserDao(ds);
      TestHelper.executeScript(ds, "create_hhuser.sql");
    }


  @BeforeEach
  public void cleanUpDb() {
    userDao.deleteAll();
  }

  @Test
  public void getAllUsersShouldReturnTwoEntities() {
    TestHelper.executeScript(ds, "insert_some_users.sql");
    Set<User> users = userDao.getAll();
    assertEquals(2, users.size());
    assertTrue(
        users
            .stream()
            .anyMatch(u -> u.getFirstName().equals("John"))
    );
  }

  @Test
  public void saveNewUserShouldInsertDbRow() {
    User user = User.newUser("John", "Lennon");
    userDao.saveNew(user);
    assertEquals(Set.of(user), userDao.getAll());
  }

  @Test
  public void savingOfExistingUserShouldBePrevented() {
    User user = User.newUser("John", "Lennon");
    userDao.saveNew(user);
    assertThrows(IllegalArgumentException.class, () -> userDao.saveNew(user));
  }

  @Test
  public void getByIdShouldReturnUserIfRowExists() {
    User user = User.newUser("John", "Lennon");
    userDao.saveNew(user);

    Optional<User> extractedUser = userDao.getBy(user.getId());

    assertTrue(extractedUser.isPresent());
    assertEquals(user, extractedUser.get());
  }


  @Test
  public void getByIdShouldReturnEmptyIfRowDoesntExist() {
    assertFalse(userDao.getBy(-1).isPresent());
  }

  @Test
  public void deleteUserShouldDeleteDbRow() {
    User user = User.newUser("John", "Lennon");
    userDao.saveNew(user);

    Optional<User> extractedUser = userDao.getBy(user.getId());
    assertTrue(extractedUser.isPresent());

    userDao.deleteBy(user.getId());

    extractedUser = userDao.getBy(user.getId());
    assertFalse(extractedUser.isPresent());
  }

  @Test
  public void updateShouldThrowExceptionForNewUsers() {
    User user = User.newUser("John", "Lennon");
    assertThrows(IllegalArgumentException.class, () -> userDao.update(user));
  }

  @Test
  public void updateShouldUpdateDbRowOfExistingUser() {
    User user = User.newUser("Ringo", "Lennon");
    userDao.saveNew(user);

    user.setFirstName("John");
    userDao.update(user);

    assertEquals(
      "John",
      userDao.getBy(user.getId()).map(User::getFirstName).orElse(null)
    );
  }



}
