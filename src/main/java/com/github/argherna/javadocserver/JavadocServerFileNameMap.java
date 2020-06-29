package com.github.argherna.javadocserver;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.util.Objects.nonNull;
import static java.util.Objects.isNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

class JavadocServerFileNameMap implements FileNameMap {

    private static final System.Logger LOGGER = System.getLogger(
            JavadocServerFileNameMap.class.getName(),
            ResourceBundle.getBundle("com.github.argherna.javadocserver.messages_javadocserver"));

    private static final String SYS_PROP_CONTENT_TYPES =
            "com.github.argherna.javadocserver.content.types.user.table";

    private static final String DEFAULT_CONTENT_TYPES_STREAM_NAME =
            "/com/github/argherna/javadocserver/content-types.properties";

    private static JavadocServerFileNameMap instance;

    private final Map<String, ContentTypeEntry> contentTypes;

    private JavadocServerFileNameMap() {
        contentTypes = new ConcurrentHashMap<>();
        try (var is = getContentTypesInputStream()) {
            var props = new Properties();
            props.load(is);
            for (var key : props.stringPropertyNames()) {
                var contentTypeEntry = toContentTypeEntry(key, props.getProperty(key));
                for (var extension : contentTypeEntry.getExtensions()) {
                    contentTypes.put(extension, contentTypeEntry);
                }
            }
        } catch (IOException e) {
            LOGGER.log(INFO, "logger.info.filetypesloadfail", e);
            contentTypes.put(".css", new ContentTypeEntry("text/css", null, List.of(".css"), null,
                    ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".gif", new ContentTypeEntry("image/gif", null, List.of(".gif"), null,
                    ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".html", new ContentTypeEntry("text/html", null, List.of(".html"),
                    null, ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".jpg", new ContentTypeEntry("image/jpeg", null, List.of(".jpg"), null,
                    ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".js", new ContentTypeEntry("application/javascript", null,
                    List.of(".js"), null, ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".png", new ContentTypeEntry("image/png", null, List.of(".png"), null,
                    ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".svg", new ContentTypeEntry("image/svg+xml", null, List.of(".svg"),
                    null, ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".woff", new ContentTypeEntry("font/woff", null,
                    List.of(".woff"), null, ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".woff2", new ContentTypeEntry("font/woff2", null,
                    List.of(".woff2"), null, ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".eot", new ContentTypeEntry("application/vnd.ms-fontobject", null,
                    List.of(".eot"), null, ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".ttf", new ContentTypeEntry("font/ttf", null,
                    List.of(".ttf"), null, ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".otf", new ContentTypeEntry("font/otf", null,
                    List.of(".otf"), null, ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".xml", new ContentTypeEntry("text/xml", null, List.of(".xml"), null,
                    ContentTypeEntry.Action.UNKNOWN, null));
            contentTypes.put(".zip", new ContentTypeEntry("application/zip", null, List.of(".zip"),
                    null, ContentTypeEntry.Action.UNKNOWN, null));
            LOGGER.log(INFO, "logger.info.filetypes.fallback", contentTypes.toString());
        }
    }

    private InputStream getContentTypesInputStream() throws IOException {
        var userTableFilename = AccessController.doPrivileged(
                (PrivilegedAction<String>) () -> System.getProperty(SYS_PROP_CONTENT_TYPES));
        if (LOGGER.isLoggable(DEBUG)) {
            var fname = nonNull(userTableFilename) ? userTableFilename
                    : DEFAULT_CONTENT_TYPES_STREAM_NAME;
            LOGGER.log(DEBUG, "logger.finer.filetypesfilename", fname);
        }
        return nonNull(userTableFilename) ? new FileInputStream(new File(userTableFilename))
                : JavadocServerFileNameMap.class
                        .getResourceAsStream(DEFAULT_CONTENT_TYPES_STREAM_NAME);
    }

    private ContentTypeEntry toContentTypeEntry(String key, String value) {
        var mimetype = key;
        var components = value.split(";");
        var description = "";
        List<String> extensions = List.of();
        var icon = "";
        var action = ContentTypeEntry.Action.UNKNOWN;
        var application = "";
        for (var component : components) {
            if (component.startsWith("description=")) {
                description = component.split("=")[1];
            } else if (component.startsWith("file_extensions=")) {
                extensions = Arrays.asList(component.split("=")[1].split(","));
            } else if (component.startsWith("icon=")) {
                icon = component.split("=")[1];
            } else if (component.startsWith("action=")) {
                var actionname = component.split("=")[1].toUpperCase();
                action = ContentTypeEntry.Action.valueOf(actionname);
            } else if (component.startsWith("application=")) {
                application = component.split("=")[1];
            }
        }
        if (description.isEmpty()) {
            description = null;
        }
        if (icon.isEmpty()) {
            icon = null;
        }
        if (application.isEmpty()) {
            application = null;
        }

        return new ContentTypeEntry(mimetype, description, extensions, icon, action, application);
    }

    static FileNameMap getInstance() {
        if (isNull(instance)) {
            instance = new JavadocServerFileNameMap();
        }
        return instance;
    }

    @Override
    public String getContentTypeFor(String fileName) {
        if (fileName.lastIndexOf(".") == -1) {
            LOGGER.log(INFO, "logger.finer.nofileext", fileName);
            return "application/octet-stream";
        }
        var ext = fileName.substring(fileName.lastIndexOf("."));
        var contentTypeEntry = contentTypes.get(ext);
        if (isNull(contentTypeEntry)) {
            LOGGER.log(INFO, "logger.info.nocontenttype", fileName);
        }
        return nonNull(contentTypeEntry) ? contentTypeEntry.getMimetype() : null;
    }

}
