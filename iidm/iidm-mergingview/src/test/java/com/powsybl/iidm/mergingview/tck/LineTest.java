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
import com.powsybl.iidm.network.tck.AbstractLineTest;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class LineTest extends AbstractLineTest {

    @Override
    protected Network createNetwork() {
        Network network = MergingView.create("merged", "test");
        network.merge(NoEquipmentNetworkFactory.create());
        return network;
    }

    @Test
    public void testChangesNotification() {
        try {
            super.testChangesNotification();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void testRemoveAcLine() {
        try {
            super.testRemoveAcLine();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void testTieLineAdder() {
        try  {
            super.testTieLineAdder();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void invalidHalfLineCharacteristicsR() {
        try  {
            createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, Double.NaN, 2.0,
                    3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "code");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void invalidHalfLineCharacteristicsX() {
        try  {
            createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, Double.NaN,
                    3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "code");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void invalidHalfLineCharacteristicsG1() {
        try  {
            createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                    Double.NaN, 3.5, 4.0, 4.5, 5.0, 6.0, "code");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void invalidHalfLineCharacteristicsG2() {
        try  {
            createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                    3.0, Double.NaN, 4.0, 4.5, 5.0, 6.0, "code");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void invalidHalfLineCharacteristicsB1() {
        try  {
            createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                    3.0, 3.5, Double.NaN, 4.5, 5.0, 6.0, "code");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void invalidHalfLineCharacteristicsB2() {
        try  {
            createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                    3.0, 3.5, 4.0, Double.NaN, 5.0, 6.0, "code");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void invalidHalfLineCharacteristicsP() {
        try  {
            createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                    3.0, 3.5, 4.0, 4.5, Double.NaN, 6.0, "code");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void invalidHalfLineCharacteristicsQ() {
        try  {
            createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                    3.0, 3.5, 4.0, 4.5, 5.0, Double.NaN, "code");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void halfLineIdNull() {
        try  {
            createTieLineWithHalfline2ByDefault(INVALID, INVALID, null, 1.0, 2.0,
                    3.0, 3.5, 4.0, 4.5, 5.0, 6.0, "code");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void uctecodeNull() {
        try  {
            createTieLineWithHalfline2ByDefault(INVALID, INVALID, INVALID, 1.0, 2.0,
                    3.0, 3.5, 4.0, 4.5, 5.0, 6.0, null);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void duplicate() {
        try  {
            super.duplicate();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }

    @Test
    public void testRemove() {
        try  {
            super.testRemove();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Not implemented exception", e.getMessage());
        }
    }
}
