package ru.hh.school.users.user;

import java.util.stream.Collectors;
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
    return session().createQuery("from User", User.class)
        .stream()
        .collect(Collectors.toSet());
  }

  //region already implemented methods

  public void saveNew(User user) {
    session().persist(user);
    // или session().merge()
    // вставляет новую строчку в БД, даже если у user уже есть id
    // persist при этом бросает исключение
  }

  public Optional<User> getBy(int id) {
    return Optional.ofNullable(
        session().get(User.class, id)
    );
  }

  public void deleteBy(int id) {
    session().createMutationQuery("delete from User u where u.id = :userId")
        .setParameter("userId", id)
        .executeUpdate();
  }

  public void deleteAll() {
    session().createMutationQuery("delete from User").executeUpdate();
  }

  public void update(User user) {
    session().merge(user);
  }

  //region relations demo (
  public User getUserByIdWithResumes(int id) {
    return session().createQuery(
        "select u from User u join fetch u.resumes where u.id=:id"
            , User.class)
        .setParameter("id", id)
        .uniqueResult();
  }
  //endregion

  //endregion

  private Session session() {
    return sessionFactory.getCurrentSession();
  }
}
