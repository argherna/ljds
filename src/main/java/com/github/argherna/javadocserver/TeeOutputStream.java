package com.github.argherna.javadocserver;

import java.io.IOException;
import java.io.OutputStream;

class TeeOutputStream extends OutputStream {

    private final OutputStream out;

    private final OutputStream branch;

    TeeOutputStream(OutputStream out, OutputStream branch) {
        this.out = out;
        this.branch = branch;
    }
    
    @Override
    public void close() throws IOException {
        out.close();
        branch.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
        branch.flush();
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        branch.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        branch.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        branch.write(b, off, len);
    }
}