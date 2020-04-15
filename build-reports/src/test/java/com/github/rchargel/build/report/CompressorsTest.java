package com.github.rchargel.build.report;

import com.github.rchargel.build.common.ClasspathUtil;
import com.github.rchargel.build.report.compressors.TextCompressor;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CompressorsTest {
    @Test
    public void compressJavascriptTest() throws IOException {
        final TextCompressor compressor = new TextCompressor();
        try (final Reader input = ClasspathUtil.readFromClasspath("report.js")) {
            final String output = compressor.compressJavaScript(input);
            assertNotNull(output);
            assertTrue(output.length() > 1);
            assertTrue(!output.contains("\n"));
        }
    }

    @Test
    public void compressCSSTest() throws IOException {
        final TextCompressor compressor = new TextCompressor();
        try (final Reader input = ClasspathUtil.readFromClasspath("report.css")) {
            final String output = compressor.compressCSS(input);
            assertNotNull(output);
            assertTrue(output.length() > 1);
            assertTrue(!output.contains("\n"));
        }
    }
}
