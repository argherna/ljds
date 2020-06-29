package com.github.argherna.javadocserver;

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for this server that defy categorization as a member of a class.
 */
class Util {

    static byte[] EMPTY_BYTE_ARRAY = new byte[0];

    // Empty private constructor to prevent instantiation.
    private Util() {
    }

    /**
     * Return contents of the named file as a byte array.
     * 
     * @param name filename.
     * @return contents of the file as a byte array.
     * @throws IOException if an I/O error occurs.
     */
    static byte[] loadFile(String name) throws IOException {
        return loadFile(new File(name));
    }

    /**
     * Return the contents of the File as a byte array.
     * 
     * @param file File.
     * @return contents of the File as a byte array.
     * @throws IOException if an I/O error occurs.
     */
    static byte[] loadFile(File file) throws IOException {
        return load(new FileInputStream(file));
    }

    /**
     * Return the contents of the named classpath resource as a byte array or an empty byte array if
     * the named resource doesn't exist.
     * 
     * @param name classpath resource name.
     * @return contents of the classpath resource as a byte array or an empty byte array if named
     *         resource doesn't exist.
     * @throws IOException if an I/O error occurs.
     */
    static byte[] loadResource(String name) throws IOException {
        var namecomps = name.split("\\.");

        // For the fourhundredth time, the way you load a classpath resource is:
        //
        //    1. Start with the file separator (/).
        //    2. Specify the fully-qualified name of the resource.
        //
        var resourcePath = new StringBuilder("/");
        for (var i = 0; i < namecomps.length; i++) {
            resourcePath.append(namecomps[i]);
            if (i < namecomps.length - 1) {
                resourcePath.append("/");
            }
        }
        resourcePath.append(".html");
        var resourceStream = Util.class.getResourceAsStream(resourcePath.toString());
        return nonNull(resourceStream)
                ? load(Util.class.getResourceAsStream(resourcePath.toString()))
                : EMPTY_BYTE_ARRAY;
    }

    /**
     * Return the contents of the given InputStream as a byte array.
     * 
     * @param is InputStream.
     * @return contents of the InputStream as a byte array.
     * @throws IOException if an I/O error occurs.
     */
    static byte[] load(InputStream is) throws IOException {
        try {
            return is.readAllBytes();
        } finally {
            is.close();
        }
    }
}
