package ru.hh.school.users;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UserDao {

  private final DataSource dataSource;

  public UserDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Set<User> getAll() {
    try (Connection connection = dataSource.getConnection()) {
      try (Statement statement = connection.createStatement();
           ResultSet rs = statement.executeQuery("SELECT * FROM hhuser");) {
        Set<User> users = new HashSet<>();
        while (rs.next()) {
          users.add(
              User.existing(
                  rs.getInt("user_id"),
                  rs.getString("first_name"),
                  rs.getString("last_name")
              )
          );
        }
        return users;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Couldn't get users", e);
    }
  }

  public void saveNew(User user) {
    if (user.getId() != null) {
      throw new IllegalArgumentException("User " + user + " already exists");
    }
    try (Connection connection = dataSource.getConnection()) {
      try (PreparedStatement statement = connection.prepareStatement("INSERT INTO hhuser (first_name, last_name) VALUES (?, ?)",
       Statement.RETURN_GENERATED_KEYS)) {
        statement.setString(1, user.getFirstName());
        statement.setString(2, user.getLastName());
        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
          throw new IllegalArgumentException("User " + user + " was not created");
        }
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            user.setId(generatedKeys.getInt(1));
          } else {
            throw new SQLException("Creating user failed, no ID generated");
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Can't insert new user", e);
    }
  }

  public void deleteAll() {
    try (Connection connection = dataSource.getConnection()) {

      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate("delete from hhuser");
      }

    } catch (SQLException e) {
      throw new RuntimeException("Can't delete all users", e);
    }
  }

  public Optional<User> getBy(int userId) {
    try (Connection connection = dataSource.getConnection()) {

      try (PreparedStatement statement = connection.prepareStatement(
        "SELECT user_id, first_name, last_name FROM hhuser WHERE user_id = ?")) {

        statement.setInt(1, userId);

        try (ResultSet resultSet = statement.executeQuery()) {

          boolean userExists = resultSet.next();
          if (!userExists) {
            return Optional.empty();
          }
          return Optional.of(
            User.existing(
              userId,
              resultSet.getString("first_name"),
              resultSet.getString("last_name")
            )
          );
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("failed to get user by id " + userId, e);
    }
  }

  public void deleteBy(int userId) {
    try (Connection connection = dataSource.getConnection()) {

      try (PreparedStatement statement = connection.prepareStatement(
        "DELETE FROM hhuser WHERE user_id = ?")) {

        statement.setInt(1, userId);

        statement.executeUpdate();
      }

    } catch (SQLException e) {
      throw new RuntimeException("failed to remove user by id " + userId, e);
    }
  }

  public void update(User user) {
    if (user.getId() == null) {
      throw new IllegalArgumentException("can not update " + user + " without id");
    }

    try (Connection connection = dataSource.getConnection()) {

      try (
        PreparedStatement statement = connection.prepareStatement(
            "update hhuser set first_name = ?, last_name = ? where user_id = ?"
        )
      ) {
        statement.setString(1, user.getFirstName());
        statement.setString(2, user.getLastName());
        statement.setInt(3, user.getId());
        statement.executeUpdate();
      }

    } catch (SQLException e) {
      throw new RuntimeException("failed to update " + user, e);
    }
  }
}
