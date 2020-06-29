package com.github.argherna.javadocserver;

import java.io.IOException;
import java.util.List;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * Sets the {@code "Server"} HTTP header.
 */
class ServerHeaderFilter extends Filter {

    @Override
    public String description() {
        return "Sets the Server response header";
    }

    @Override
    public void doFilter(HttpExchange exchange, Filter.Chain chain) throws IOException {
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.put("Server", List.of(JavadocServer.class.getSimpleName()));
        chain.doFilter(exchange);
    }
}