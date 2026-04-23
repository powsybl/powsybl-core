/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public abstract class AbstractBusTerminalTest {

    @Test
    public void testSetInvalidConnectableBus() {
        Network network = EurostagTutorialExample1Factory.create();
        Terminal.BusBreakerView busBreakerView = network.getLoad("LOAD").getTerminal().getBusBreakerView();
        assertThrows(PowsyblException.class, () -> busBreakerView.setConnectableBus("UNKNOWN"));
    }
}
