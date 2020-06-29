package com.github.argherna.javadocserver;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

class DocsHandler extends JavadocHandler {

    private final String contentTypeHtml =
            JavadocServerFileNameMap.getInstance().getContentTypeFor(".html");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        doSend(exchange, contentTypeHtml, gettingStarted, HTTP_OK);
    }
}
