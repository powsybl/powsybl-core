/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractNodeBreakerInternalConnectionsTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class NodeBreakerInternalConnectionsTest extends AbstractNodeBreakerInternalConnectionsTest {

    @Override
    protected Network createNetwork(String id, String format) {
        return MergingView.create(id, format);
    }

    @Test
    public void testTraversalInternalConnections() {
        try {
            super.testTraversalInternalConnections();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void testRemoveInternalConnections() {
        try {
            super.testRemoveInternalConnections();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void testRemoveVoltageLevelWithInternalConnectionsIssue() {
        try {
            super.testRemoveVoltageLevelWithInternalConnectionsIssue();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }
}
