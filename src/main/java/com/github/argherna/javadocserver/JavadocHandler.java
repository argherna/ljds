package com.github.argherna.javadocserver;

import static com.github.argherna.javadocserver.Util.loadResource;
import static java.lang.String.format;
import static java.lang.System.Logger.Level.DEBUG;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.zip.ZipFile;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Base HttpHandler implementation for rendering javadoc pages from an archive file.
 */
abstract class JavadocHandler implements HttpHandler {

    static final Integer HTTP_OK = 200;

    static final Integer HTTP_BAD_REQUEST = 400;

    static final Integer HTTP_NOT_FOUND = 404;

    private static final System.Logger LOGGER = System.getLogger(JavadocHandler.class.getName());

    private static final String DOCS_PAGE = "com.github.argherna.javadocserver.html.docs";

    private static final String BAD_REQUEST_PAGE = "com.github.argherna.javadocserver.html.400";

    private static final String NOT_FOUND_PAGE = "com.github.argherna.javadocserver.html.404";

    static final String SYSPROP_FILE_SEP = AccessController
            .doPrivileged((PrivilegedAction<String>) () -> System.getProperty("file.separator"));

    final byte[] badRequest;

    final byte[] gettingStarted;

    final byte[] notFound;

    final String contentTypeHtml = JavadocServerFileNameMap.getInstance().getContentTypeFor(".html");

    JavadocHandler() {
        try {
            badRequest = loadResource(BAD_REQUEST_PAGE);
            gettingStarted = loadResource(DOCS_PAGE);
            notFound = loadResource(NOT_FOUND_PAGE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void doSend(HttpExchange exchange, String contentType, String archiveName, String entryName)
            throws IOException {
        var h = exchange.getResponseHeaders();
        h.add("Content-Type", contentType);
        var contentLength = -1;
        InputStream source;
        var status = HTTP_OK;
        try (var zf = new ZipFile(archiveName)) {
            var ze = zf.getEntry(entryName);
            if (ze != null) {
                contentLength = Long.valueOf(ze.getSize()).intValue();
                source = zf.getInputStream(ze);
            } else {
                h.remove("Content-Type");
                h.add("Content-Type", contentTypeHtml);
                status = HTTP_NOT_FOUND;
                byte[] content = notFound;
                source = new ByteArrayInputStream(content);
                contentLength = content.length;
            }
            exchange.sendResponseHeaders(status, contentLength);
            source.transferTo(exchange.getResponseBody());
        }
        exchange.close();
    }

    void doSend(HttpExchange exchange, String contentType, byte[] content, int status)
            throws IOException {
        var h = exchange.getResponseHeaders();
        h.add("Content-Type", contentType);

        var contentLength = (content.length == 0) ? -1 : content.length;
        LOGGER.log(DEBUG, format("[status=%d, contentType=%s, contentLength=%d]", status,
                contentType, contentLength));
        exchange.sendResponseHeaders(status, contentLength);
        if (contentLength > 0) {
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(content);
                out.flush();
            }
        }
        exchange.close();
    }
}
