package com.github.argherna.javadocserver;

import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FileVisitor that looks for directories containing jar files with a name that matches
 * {@code *-javadoc.jar}.
 */
class JavadocArchiveFinder extends SimpleFileVisitor<Path> {

  private static final System.Logger LOGGER = System.getLogger(JavadocArchiveFinder.class.getName(),
      ResourceBundle.getBundle("com.github.argherna.javadocserver.messages_javadocserver"));

  private final List<String> javadocArtifactDirectoryNames = new ArrayList<>();

  private final PathMatcher matcher =
      FileSystems.getDefault().getPathMatcher("regex:.*-javadoc.jar$");

  private final Path basePath;

  JavadocArchiveFinder(Path basePath) {
    this.basePath = basePath;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    LOGGER.log(TRACE, "logger.finer.scanning", dir.toString());
    return super.preVisitDirectory(dir, attrs);
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    var name = file.getFileName();
    if (name != null && matcher.matches(name)) {
      var subpath = file.subpath(basePath.getNameCount(), file.getNameCount() - 1);
      LOGGER.log(TRACE, "logger.finer.adding", subpath.toString());
      javadocArtifactDirectoryNames.add(subpath.toString());
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    LOGGER.log(WARNING, "logger.warning.visitfail", exc);
    return super.visitFileFailed(file, exc);
  }

  Collection<String> getJavadocArtifactDirectoryNames() {
    return javadocArtifactDirectoryNames;
  }
}
