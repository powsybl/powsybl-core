/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.tools;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractToolTest {

    protected FileSystem fileSystem;

    private CommandLineTools tools;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tools = new CommandLineTools(getTools());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    protected abstract Iterable<Tool> getTools();

    protected void assertMatches(String expected, String actual) {
        if (!actual.equals(expected) && !Pattern.compile(expected).matcher(actual).find()) {
            throw new ComparisonFailure("", expected, actual);
        }
    }

    protected void assertCommand(String[] args, int expectedStatus, String expectedOut, String expectedErr) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        int status;
        try (PrintStream out = new PrintStream(bout);
             PrintStream err = new PrintStream(berr)) {
            status = tools.run(args, new ToolRunningContext(out, err, fileSystem));
        }
        assertEquals(expectedStatus, status);
        assertMatches(expectedOut, bout.toString(StandardCharsets.UTF_8.name()));
        assertMatches(expectedErr, berr.toString(StandardCharsets.UTF_8.name()));
    }

}