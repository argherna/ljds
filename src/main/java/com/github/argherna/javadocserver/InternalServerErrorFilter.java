package com.github.argherna.javadocserver;

import static com.github.argherna.javadocserver.Util.loadResource;
import static java.lang.System.Logger.Level.WARNING;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * Catches RuntimeExceptions to log them and return a 500 status.
 */
class InternalServerErrorFilter extends Filter {

    private static final Integer HTTP_INTERNAL_SERVER_ERROR = 500;

    private static final String INTERNAL_SERVER_ERROR_PAGE =
            "com.github.argherna.javadocserver.html.500";

    private static final System.Logger LOGGER = System.getLogger(
            InternalServerErrorFilter.class.getName(),
            ResourceBundle.getBundle("com.github.argherna.javadocserver.messages_javadocserver"));

    private final byte[] content;

    private final String contentTypeHtml = JavadocServerFileNameMap.getInstance().getContentTypeFor(".html");

    InternalServerErrorFilter() {
        try {
            content = loadResource(INTERNAL_SERVER_ERROR_PAGE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String description() {
        return "Handles runtime exceptions by logging an error and setting the HTTP status to 500";
    }

    @Override
    public void doFilter(HttpExchange exchange, Filter.Chain chain) throws IOException {
        try {
            chain.doFilter(exchange);
        } catch (RuntimeException e) {

            // Get the root cause for logging.
            var cause = e.getCause();
            var c0 = cause;
            while (c0 != null) {
                c0 = cause.getCause();
                if (c0 != null) {
                    cause = c0;
                }
            }
            LOGGER.log(WARNING, "logger.warning.ise", cause);
            var responseHeaders = exchange.getResponseHeaders();
            responseHeaders.put("Content-Type", List.of(contentTypeHtml));
            exchange.sendResponseHeaders(HTTP_INTERNAL_SERVER_ERROR, content.length);
            try (var out = exchange.getResponseBody()) {
                out.write(content);
                out.flush();
            } finally {
                exchange.close();
            }
        }
    }
}
