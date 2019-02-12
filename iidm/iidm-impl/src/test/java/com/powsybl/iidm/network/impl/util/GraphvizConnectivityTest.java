/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.google.common.io.ByteStreams;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.GraphvizConnectivity;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GraphvizConnectivityTest {

    @Test
    public void test() throws IOException, NoSuchAlgorithmException {
        Network network = EurostagTutorialExample1Factory.create();
        try (StringWriter writer = new StringWriter()) {
            new GraphvizConnectivity(network, new Random(0)).write(writer);
            writer.flush();
            assertEquals(new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/eurostag-tutorial-example1.dot")), StandardCharsets.UTF_8),
                         writer.toString());
        }
    }
}
