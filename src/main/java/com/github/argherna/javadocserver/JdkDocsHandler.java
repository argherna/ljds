package com.github.argherna.javadocserver;

import java.io.IOException;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;

/**
 * Renders Javadoc pages from JDK documentation archives.
 */
class JdkDocsHandler extends JavadocHandler {

    private static final int AFTER_PATH_AND_KEY_IDX = 2;

    private static final int MIN_PATH_ELEMENTS = 3;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var pathElements = Arrays.asList(exchange.getRequestURI().getPath().split(SYSPROP_FILE_SEP))
                .stream().filter(pe -> pe != null && !pe.isEmpty()).collect(Collectors.toList());
        var contentType = contentTypeHtml;
        var javadocArchiveName = Preferences.userNodeForPackage(ServerMain.class)
                .node("jdk-docs").get(pathElements.get(1), "");
        if (javadocArchiveName.isEmpty()) {
            doSend(exchange, contentType, notFound, HTTP_NOT_FOUND);
        } else if (pathElements.size() < MIN_PATH_ELEMENTS) {
            doSend(exchange, contentType, badRequest, HTTP_BAD_REQUEST);
        } else {
            var sj = new StringJoiner(SYSPROP_FILE_SEP);
            pathElements.subList(AFTER_PATH_AND_KEY_IDX, pathElements.size()).stream()
                    .forEach(p -> sj.add(p));
            var filename = sj.toString();
            contentType = JavadocServerFileNameMap.getInstance().getContentTypeFor(filename);
            doSend(exchange, contentType, javadocArchiveName, filename);
        }
    }
}
