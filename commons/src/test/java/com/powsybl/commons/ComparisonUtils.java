package com.powsybl.commons;

import com.google.common.io.ByteStreams;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.transform.Source;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ComparisonUtils {
    protected static void compareXml(InputStream expected, InputStream actual) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        Diff myDiff = DiffBuilder.compare(control).withTest(test).ignoreWhitespace().ignoreComments().build();
        boolean hasDiff = myDiff.hasDifferences();
        if (hasDiff) {
            System.err.println(myDiff.toString());
        }
        assertFalse(hasDiff);
    }

    protected static void compareTxt(InputStream expected, InputStream actual) {
        try {
            compareTxt(expected, new String(ByteStreams.toByteArray(actual), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected static void compareTxt(InputStream expected, InputStream actual, List<Integer> excludedLines) {
        BufferedReader expectedReader = new BufferedReader(new InputStreamReader(expected));
        List<String> expectedLines = expectedReader.lines().collect(Collectors.toList());
        BufferedReader actualReader = new BufferedReader(new InputStreamReader(actual));
        List<String> actualLines = actualReader.lines().collect(Collectors.toList());

        for (int i = 0; i < expectedLines.size(); i++) {
            if (!excludedLines.contains(i)) {
                assertEquals(expectedLines.get(i), actualLines.get(i));
            }
        }
    }

    protected static void compareTxt(InputStream expected, String actual) {
        try {
            String expectedStr = TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(expected), StandardCharsets.UTF_8));
            String actualStr = TestUtil.normalizeLineSeparator(actual);
            assertEquals(expectedStr, actualStr);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
