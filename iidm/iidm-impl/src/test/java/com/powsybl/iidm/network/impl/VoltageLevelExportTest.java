/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.io.ByteStreams;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VoltageLevelExportTest {

    @Test
    public void nodeBreakerTest() throws IOException {
        Network network = FictitiousSwitchFactory.create();
        try (StringWriter writer = new StringWriter()) {
            network.getVoltageLevel("C").exportTopology(writer, new Random(0));
            writer.flush();
            assertEquals(new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/fictitious-switch-c.dot")), StandardCharsets.UTF_8),
                         writer.toString());
        }
    }

    @Test
    public void busBreakerTest() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        try (StringWriter writer = new StringWriter()) {
            network.getVoltageLevel("VLHV1").exportTopology(writer, new Random(0));
            writer.flush();
            assertEquals(new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/eurostag-tutorial-example1-vlhv1.dot")), StandardCharsets.UTF_8),
                         writer.toString());
        }
    }
}
