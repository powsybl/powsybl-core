/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.GraphvizConnectivity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GraphvizConnectivityTest extends AbstractConverterTest {

    @Test
    void test() throws IOException, NoSuchAlgorithmException {
        Network network = EurostagTutorialExample1Factory.create();
        try (StringWriter writer = new StringWriter()) {
            new GraphvizConnectivity(network, new Random(0)).write(writer);
            writer.flush();
            ComparisonUtils.compareTxt(getClass().getResourceAsStream("/eurostag-tutorial-example1.dot"), writer.toString());
        }
    }

    @Test
    void testCountryCluster() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        try (StringWriter writer = new StringWriter()) {
            new GraphvizConnectivity(network, new Random(0)).setCountryCluster(true).write(writer);
            writer.flush();
            String dot = writer.toString().replaceAll("\\s+// scope=(.*)", ""); // to remove unstable comments
            ComparisonUtils.compareTxt(getClass().getResourceAsStream("/eurostag-tutorial-country-cluster.dot"), dot);
        }
    }
}
