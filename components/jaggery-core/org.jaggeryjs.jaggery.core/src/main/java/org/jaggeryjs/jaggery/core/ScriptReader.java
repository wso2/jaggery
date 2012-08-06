package org.jaggeryjs.jaggery.core;

import org.jaggeryjs.hostobjects.jaggeryparser.JaggeryParser;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import java.io.*;

public class ScriptReader extends Reader {

    protected InputStream sourceIn = null;
    protected Reader sourceReader = null;
    private boolean isBuilt = false;

    public ScriptReader(InputStream sourceIn) {
        this.sourceIn = sourceIn;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if(!isBuilt) {
            build();
            isBuilt = true;
        }
        return sourceReader.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        sourceReader.close();
    }

    protected void build() throws IOException {
        try {
            sourceReader = new BufferedReader(new InputStreamReader(JaggeryParser.parse(sourceIn)));
        } catch (ScriptException e) {
            throw new IOException(e);
        }
    }
}
