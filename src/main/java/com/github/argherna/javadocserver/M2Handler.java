package com.github.argherna.javadocserver;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.DEBUG;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;

/**
 * Renders Javadoc pages from archives in the maven repositories.
 */
class M2Handler extends JavadocHandler {

    private static final Integer MIN_PATH_ELEMENTS = 4;

    private static final System.Logger LOGGER = System.getLogger(M2Handler.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var m2Repos = Preferences.userNodeForPackage(ServerMain.class).node("m2-repos");
        var m2RepoPath = m2Repos.get("default", "");
        var pathElements = Arrays.stream(exchange.getRequestURI().getPath().split(SYSPROP_FILE_SEP))
                .filter(pe -> pe != null && !pe.isEmpty()).collect(Collectors.toList());

        if (m2RepoPath.isEmpty()) {
            doSend(exchange, contentTypeHtml, gettingStarted, HTTP_OK);
        } else if (pathElements.size() < MIN_PATH_ELEMENTS) {
            doSend(exchange, contentTypeHtml, badRequest, HTTP_BAD_REQUEST);
        } else {
            var filename =
                    toMavenCoordinates(m2RepoPath, pathElements.subList(1, pathElements.size()));
            var fpJoiner = new StringJoiner(SYSPROP_FILE_SEP);
            pathElements.subList(MIN_PATH_ELEMENTS, pathElements.size()).stream()
                    .forEach(pe -> fpJoiner.add(pe));
            var docPath = fpJoiner.toString();
            LOGGER.log(DEBUG, () -> format("[filename=%s, docPath=%s]", filename, docPath));
            doSend(exchange, JavadocServerFileNameMap.getInstance().getContentTypeFor(docPath), filename, docPath);
        }
    }

    private String toMavenCoordinates(String baseDirname, List<String> components) {
        return new StringJoiner(SYSPROP_FILE_SEP).add(baseDirname)
                .add(components.get(0).replace(".", SYSPROP_FILE_SEP)).add(components.get(1))
                .add(components.get(2)).add(new StringJoiner("-").add(components.get(1))
                        .add(components.get(2)).add("javadoc.jar").toString())
                .toString();
    }

}
