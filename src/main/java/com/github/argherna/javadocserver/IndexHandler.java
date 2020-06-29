package com.github.argherna.javadocserver;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.DEBUG;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.sun.net.httpserver.HttpExchange;

/**
 * Renders the index page or a page of documentation explaining how to get started.
 */
class IndexHandler extends JavadocHandler {

    private static final String HTML_LIST_ITEM_TEMPLATE =
            "<li><a href=\"/m2/%1$s/%2$s/%3$s/index.html\">%1$s:%2$s:%3$s</a></li>";

    private static final String INDEX_HTML_TEMPLATE = "<!DOCTYPE html><html><h"
            + "ead><link href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/c"
            + "ss/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-9gVQ4dYF"
            + "wwWSjIDZnLEWnxCjeSWFphJiwGPXr1jddIhOegiu1FwO5qRGvFXOdJZ4\" crossorigi"
            + "n=\"anonymous\"><title>Available Local Javadoc</title></head><body><d"
            + "iv class=\"container\"><h1>Avaliable Local Javadoc</h1><h2>JDK API</h"
            + "2><ul>%s</ul><h2>Maven Repository</h2><ul>%s</ul></div><script src=\""
            + "https://stackpath.bootstrapcdn.com/bootstrap/4.1.0/js/bootstrap.min.j"
            + "s\" integrity=\"sha384-uefMccjFJAIv6A+rW+L4AHf99KvxDjWSu1z9VI8SKNVmz4"
            + "sk7buKt/6v9KI65qnm\" crossorigin=\"anonymous\"></script></body></html" + ">";

    private static final System.Logger LOGGER = System.getLogger(IndexHandler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var content = new byte[0];
        var status = HTTP_OK;
        var javadocServer = Preferences.userNodeForPackage(JavadocServer.class);
        try {
            if (javadocServer.childrenNames().length == 0) {
                content = gettingStarted;
            } else if (exchange.getRequestURI().getPath().equals("/favicon.ico")) {
                status = HTTP_NOT_FOUND;
                content = notFound;
            } else {
                var m2Repos = javadocServer.node("m2-repos");
                var m2RepoPath = m2Repos.get("default", "");
                var artifactDirs = getMavenJavadocArtifactDirectoryNames(m2RepoPath);
                LOGGER.log(DEBUG, artifactDirs.toString());
                content = format(INDEX_HTML_TEMPLATE,
                        renderJdkDocsListItems(javadocServer.node("jdk-docs").keys()),
                        toHtmlListElements(artifactDirs)).getBytes();
            }
            doSend(exchange, contentTypeHtml, content, status);
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private String renderJdkDocsListItems(String[] keys) throws BackingStoreException {
        var sb = new StringBuilder();
        for (var key : keys) {
            sb.append("<li><a href=\"/jdk/").append(key).append("/docs/api/index.html\">Java ")
                    .append(key).append("</a></li>");
        }
        return sb.toString();
    }

    private Iterable<String> getMavenJavadocArtifactDirectoryNames(String repoDir)
            throws IOException {
        var mavenRepoDir = Paths.get(repoDir);
        var dirnames = new ArrayList<String>();
        if (mavenRepoDir.toFile().exists()) {
            var finder = new JavadocArchiveFinder(mavenRepoDir);
            Files.walkFileTree(mavenRepoDir, finder);
            dirnames.addAll(finder.getJavadocArtifactDirectoryNames());
        }
        return dirnames;
    }

    private String toHtmlListElements(Iterable<String> javadocArchiveNames) {
        var listJoiner = new StringJoiner("");
        for (var javadocArchivename : javadocArchiveNames) {
            var parts = Arrays.asList(javadocArchivename.split(SYSPROP_FILE_SEP));
            var dotJoiner = new StringJoiner(".");
            parts.subList(0, parts.size() - 2).stream().forEach(part -> dotJoiner.add(part));
            var listElement = format(HTML_LIST_ITEM_TEMPLATE, dotJoiner.toString(),
                    parts.get(parts.size() - 2), parts.get(parts.size() - 1));
            listJoiner.add(listElement);
        }
        return listJoiner.toString();
    }
}
