package ru.hh.school.users.user;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Optional;
import java.util.Set;

public class UserDao {

  private final SessionFactory sessionFactory;

  public UserDao(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public Set<User> getAll() {
    //TODO: implement
    return null;
  }

  public void saveNew(User user) {
    //TODO: implement
  }

  public Optional<User> getBy(int id) {
    return Optional.ofNullable(
        session().get(User.class, id)
    );
  }

  public void deleteBy(int id) {
    //TODO: implement
  }

  public void deleteAll() {
    session().createQuery("delete from User").executeUpdate();
  }

  public void update(User user) {
    //TODO: implement
  }

  private Session session() {
    return sessionFactory.getCurrentSession();
  }
}
