package com.github.argherna.javadocserver;

import static com.github.argherna.javadocserver.Util.loadResource;

import java.io.IOException;
import java.util.List;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * Responds with a 405 status for any method besides {@code GET} or {@code HEAD}.
 */
class AllowGetRequestsOnlyFilter extends Filter {

    private static final Integer HTTP_METHOD_NOT_ALLOWED = 405;

    private static final String METHOD_NOT_ALLOWED_PAGE =
            "com.github.argherna.javadocserver.html.405";

    private final byte[] content;

    private final String contentTypeHtml =
            JavadocServerFileNameMap.getInstance().getContentTypeFor(".html");

    AllowGetRequestsOnlyFilter() {
        try {
            content = loadResource(METHOD_NOT_ALLOWED_PAGE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String description() {
        return "Allows GET requests only.";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            var responseHeaders = exchange.getResponseHeaders();
            responseHeaders.put("Allow", List.of("GET"));
            responseHeaders.put("Content-Type", List.of(contentTypeHtml));
            exchange.sendResponseHeaders(HTTP_METHOD_NOT_ALLOWED, content.length);
            try (var out = exchange.getResponseBody()) {
                out.write(content);
                out.flush();
            } finally {
                exchange.close();
            }
        } else {
            chain.doFilter(exchange);
        }
    }
}
