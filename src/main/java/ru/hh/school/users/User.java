package ru.hh.school.users;

import java.util.Objects;

public class User {

  private Integer id;
  private String firstName;
  private String lastName;

  private User(Integer id, String firstName, String lastName) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  static User existing(int id, String firstName, String lastName) {
    return new User(id, firstName, lastName);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    User user = (User) o;
    return Objects.equals(getId(), user.getId()) && Objects.equals(getFirstName(), user.getFirstName()) && Objects.equals(
        getLastName(),
        user.getLastName()
    );
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public static User newUser(String firstName, String lastName) {
    return new User(null, firstName, lastName);
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Override
  public String toString() {
    return "User{" +
      "id=" + id +
      ", firstName='" + firstName + '\'' +
      ", lastName='" + lastName + '\'' +
      '}';
  }

}
