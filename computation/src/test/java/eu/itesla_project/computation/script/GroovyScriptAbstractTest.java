/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.script;

import eu.itesla_project.computation.ComputationManager;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class GroovyScriptAbstractTest {

    protected abstract Reader getCodeReader();

    protected abstract String getExpectedOutput();

    protected List<GroovyScriptExtension> getExtensions() {
        return Collections.emptyList();
    }

    @Test
    public void test() throws IOException {
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        StringWriter out = new StringWriter();
        try (Reader codeReader = getCodeReader()) {
            GroovyScripts.run(codeReader, computationManager, getExtensions(), out);
        } finally {
            out.close();
        }
        assertEquals(out.toString(), getExpectedOutput());
    }
}
