/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.GraphvizConnectivity;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GraphvizConnectivityTest extends AbstractSerDeTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        try (StringWriter writer = new StringWriter()) {
            new GraphvizConnectivity(network, new Random(0)).write(writer);
            writer.flush();
            ComparisonUtils.assertTxtEquals(getClass().getResourceAsStream("/eurostag-tutorial-example1.dot"), writer.toString());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testCountryCluster() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        try (StringWriter writer = new StringWriter()) {
            new GraphvizConnectivity(network, new Random(0)).setCountryCluster(true).write(writer);
            writer.flush();
            String dot = writer.toString().replaceAll("\\s+// scope=(.*)", ""); // to remove unstable comments
            ComparisonUtils.assertTxtEquals(getClass().getResourceAsStream("/eurostag-tutorial-country-cluster.dot"), dot);
        } catch (Exception e) {
            fail(e);
        }
    }
}
