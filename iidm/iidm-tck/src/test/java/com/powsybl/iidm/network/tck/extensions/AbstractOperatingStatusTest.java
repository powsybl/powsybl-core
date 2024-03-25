/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.iidm.network.extensions.OperatingStatusAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractOperatingStatusTest {

    @Test
    public void test() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        Line l = network.getLine("LINE_S2S3");
        assertNotNull(l);
        l.newExtension(OperatingStatusAdder.class)
                .withStatus(OperatingStatus.Status.FORCED_OUTAGE)
                .add();
        OperatingStatus<Line> operatingStatus = l.getExtension(OperatingStatus.class);
        assertEquals("operatingStatus", operatingStatus.getName());
        assertSame(l, operatingStatus.getExtendable());
        assertSame(OperatingStatus.Status.FORCED_OUTAGE, operatingStatus.getStatus());
        operatingStatus.setStatus(OperatingStatus.Status.IN_OPERATION);
        assertSame(OperatingStatus.Status.IN_OPERATION, operatingStatus.getStatus());

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TWT");
        assertNotNull(l);
        twt.newExtension(OperatingStatusAdder.class)
                .withStatus(OperatingStatus.Status.PLANNED_OUTAGE)
                .add();

        HvdcLine hvdcl = network.getHvdcLine("HVDC1");
        hvdcl.newExtension(OperatingStatusAdder.class)
                .withStatus(OperatingStatus.Status.PLANNED_OUTAGE)
                .add();

        Generator g = network.getGenerator("GH1");
        OperatingStatusAdder operatingStatusAdder = g.newExtension(OperatingStatusAdder.class)
                .withStatus(OperatingStatus.Status.PLANNED_OUTAGE);
        PowsyblException e = assertThrows(PowsyblException.class, operatingStatusAdder::add);
        assertEquals("Operating status extension is not allowed on identifiable type: GENERATOR", e.getMessage());
    }
}
