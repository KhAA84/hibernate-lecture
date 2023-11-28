package ru.hh.school.users.resume;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ResumeDao {
  private final SessionFactory sessionFactory;

  public ResumeDao(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void saveNew(Resume resume) {
    session().persist(resume);
  }

  public Optional<Resume> getBy(int id) {
    return Optional.ofNullable(session().get(Resume.class, id));
  }

  public Set<Resume> getActiveResumesForUserId(int userId) {
    // TODO: implement
    return new HashSet<>();
  }

  private Session session() {
    return sessionFactory.getCurrentSession();
  }
}
