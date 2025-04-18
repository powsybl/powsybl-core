/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.util.GraphWriter;
import com.powsybl.iidm.network.util.GraphvizConnectivitySubstationView;
import com.powsybl.nad.NetworkAreaDiagram;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Path;

/**
 * @author Nathan Dissoubray {@literal <nathan.dissoubray at rte-france.com>}
 */
class GraphvizConnectivitySubstationViewTest extends AbstractSerDeTest {
    //TODO remove this, just for testing
    private void export_xiidm(String svgPath, Network network) {
        NetworkAreaDiagram.draw(network, Path.of(svgPath));
    }
    @Test
    void test_write() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        export_xiidm("/tmp/xiidm_test_export_2.svg", network);
        try (StringWriter writer = new StringWriter()) {
            GraphWriter gWriter = new GraphWriter(writer);
            new GraphvizConnectivitySubstationView(network).write(gWriter);
            InputStream expected = getClass().getResourceAsStream("/eurostag-tutorial-example1_simplified.dot");
            String got = gWriter.toString();
            ComparisonUtils.assertTxtEquals(expected, got);
        }
    }
}
