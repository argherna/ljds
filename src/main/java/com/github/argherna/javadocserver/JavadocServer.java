package com.github.argherna.javadocserver;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import com.sun.net.httpserver.HttpServer;

/**
 * HTTP Server that serves Javadoc found on local host.
 * 
 * <p>
 * The server assumes that a properly installed JDK and Maven are available locally. It looks up the
 * Javadoc zip file from locations specified in the user preferences. If you are running this for
 * the first time, navigate to {@link http://localhost:8084/} for getting started instructions.
 * 
 */
public class JavadocServer implements Runnable {

    private static final int DEFAULT_HTTP_PORT = 8084;

    static final System.Logger LOGGER = System.getLogger(JavadocServer.class.getName());

    private final HttpServer httpServer;

    private final int port;

    /**
     * The main takes 1 command line argument that signifies the port to run the server on.
     */
    public static void main(String... args) {

        int argIdx = 0;
        int port = DEFAULT_HTTP_PORT;

        while (argIdx < args.length) {
            String arg = args[argIdx];
            switch (arg) {
                case "-h":
                    showUsageAndExit(2);
                    break;
                default:
                    try {
                        port = Integer.valueOf(arg);
                    } catch (NumberFormatException e) {
                        LOGGER.log(INFO, format("Invalid port number %s, defaulting to %d%n", arg,
                                DEFAULT_HTTP_PORT));
                        port = DEFAULT_HTTP_PORT;
                    }
                    break;
            }
            argIdx++;
        }

        try {
            var javadocServer = new JavadocServer(port);
            javadocServer.run();
        } catch (Exception e) {
            System.err.printf("%s", e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void showUsageAndExit(int status) {
        showUsage();
        System.exit(status);
    }

    private static void showUsage() {
        System.err.printf("Usage: %s [port]%n", JavadocServer.class.getName());
        System.err.println();
        System.err
                .println("Serves content from javadoc jars stored locally in zip" + "/jar files.");
        System.err.println("Documentation paths and types are read from user " + "preferences.");
        System.err.println(
                "Start the server and open http://localhost:port for " + "how to get started.");
        System.err.println();
        System.err.println("Arguments:");
        System.err.println();
        System.err.println(" port  port the server will listen on " + "(default is "
                + DEFAULT_HTTP_PORT + ")");
        System.err.println();
        System.err.println("Options:");
        System.err.println();
        System.err.println(" -h    show this help and exit");
    }

    public JavadocServer(int port) {
        try {
            this.port = port;
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        init();
        LOGGER.log(DEBUG, "Starting HTTP server...");
        httpServer.start();
        LOGGER.log(INFO, format(
                "Welcome to JavadocServer. Visit http://localhost:%d/docs for initial setup information.",
                port));
    }

    private void init() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.log(WARNING, "Stopping HTTP server...");
                httpServer.stop(0);
            }
        });

        var filters = List.of(new AllowGetRequestsOnlyFilter(), new InternalServerErrorFilter(),
                new ServerHeaderFilter());

        var ctx0 = httpServer.createContext("/", new IndexHandler());
        ctx0.getFilters().addAll(filters);

        var ctx1 = httpServer.createContext("/jdk", new JdkDocsHandler());
        ctx1.getFilters().addAll(filters);

        var ctx2 = httpServer.createContext("/m2", new M2Handler());
        ctx2.getFilters().addAll(filters);

        var ctx3 = httpServer.createContext("/docs", new DocsHandler());
        ctx3.getFilters().addAll(filters);
    }
}
