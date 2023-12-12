package ru.hh.school.users.user;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ru.hh.school.users.TransactionHelper;

import java.util.Optional;
import java.util.Set;

public class UserService {

  private final UserDao userDao;
  private final TransactionHelper th;
  private final SessionFactory sessionFactory;

  public UserService(
    SessionFactory sessionFactory,
    UserDao userDao
  ) {
    this.userDao = userDao;
    this.th = new TransactionHelper(sessionFactory);
    this.sessionFactory = sessionFactory;
  }

  public Set<User> getAll() {
    return th.inTransaction(userDao::getAll);
  }

  public void deleteAll() {
    th.inTransaction(userDao::deleteAll);
  }

  public void saveNew(User user) {
    Session session = sessionFactory.getCurrentSession();
    Transaction transaction = session.getTransaction();
    transaction.begin();
    try {
      userDao.saveNew(user);
      transaction.commit();
    } catch (RuntimeException e) {
      transaction.rollback();
      throw e;
    } finally {
      session.close();
    }
  }

  public Optional<User> getBy(int userId) {
    return th.inTransaction(() -> userDao.getBy(userId));
  }

  public void deleteBy(int userId) {
    th.inTransaction(() -> userDao.deleteBy(userId));
  }

  public void update(User user) {
    th.inTransaction(() -> userDao.update(user));
  }

  public void changeFullName(int userId, String firstName, String lastName) {
    th.inTransaction(() -> {
      userDao.getBy(userId)
          .ifPresent(user -> {
            user.setFirstName(firstName);
            user.setLastName(lastName);
          });
      // хибер отслеживает изменения сущностей и выполняет sql update перед коммитом транзакции
    });
  }

}
