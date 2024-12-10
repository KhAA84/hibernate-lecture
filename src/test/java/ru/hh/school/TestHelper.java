package ru.hh.school;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class TestHelper {
  private static final Path SCRIPTS_DIR = Path.of("src","main", "resources", "scripts");

  /**
   * Файл должен лежать в resources/scripts
   */

  public static void executeScript(SessionFactory sessionFactory, String scriptFileName) {
    splitToQueries(SCRIPTS_DIR.resolve(scriptFileName))
        .forEach((query) -> execute(sessionFactory, query));
  }

  public static void execute(SessionFactory sessionFactory, String query) {
    try (Session session = sessionFactory.openSession()) {
      session.beginTransaction();
      session.createNativeMutationQuery(query).executeUpdate();
    }
  }

  private static Stream<String> splitToQueries(Path path) {
    try {
      return Arrays.stream(Files.readString(path).split(";"));
    } catch (IOException e) {
      throw new RuntimeException("Can't read file " + path, e);
    }
  }

}
