/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.eurostag.export;

import com.google.common.io.CharStreams;
import eu.itesla_project.eurostag.network.EsgGeneralParameters;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import eu.itesla_project.iidm.network.test.SvcTestCaseFactory;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagEchExportTest {

    private void test(Network network, String reference, LocalDate editDate) throws IOException {
        StringWriter writer = new StringWriter();
        EsgGeneralParameters parameters = new EsgGeneralParameters();
        parameters.setEditDate(editDate);
        new EurostagEchExport(network).write(writer, parameters);
        writer.close();
        assertEquals(CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream(reference), StandardCharsets.UTF_8)), writer.toString());
    }

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        test(network, "/eurostag-tutorial-example1.ech", LocalDate.parse("2016-03-01"));
    }

    @Test
    public void testSVC() throws IOException {
        Network network = SvcTestCaseFactory.create();
        test(network, "/eurostag-svc-test.ech", LocalDate.parse("2016-01-01"));
    }
}