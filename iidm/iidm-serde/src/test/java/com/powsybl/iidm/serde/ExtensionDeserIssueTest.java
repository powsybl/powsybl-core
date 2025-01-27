/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ExtensionDeserIssueTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        var load = network.getLoad("LOAD");
        var vlload = network.getVoltageLevel("VLLOAD");
        vlload.newExtension(SlackTerminalAdder.class)
                .withTerminal(load.getTerminal())
                .add();
        try (var fs = Jimfs.newFileSystem(Configuration.unix())) {
            var file = fs.getPath("/work/test.xiidm");
            network.write("XIIDM", null, file);
            Network network2 = Network.read(file);
            var vlload2 = network2.getVoltageLevel("VLLOAD");
            var slackTerminal = vlload2.getExtension(SlackTerminal.class);
            assertNotNull(slackTerminal.getTerminal().getBusView().getBus());
        }
    }
}
