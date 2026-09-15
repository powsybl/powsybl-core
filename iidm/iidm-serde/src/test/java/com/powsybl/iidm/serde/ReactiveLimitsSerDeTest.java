/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.test.ReactiveLimitsTestNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ReactiveLimitsSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("reactiveLimitsRoundTripRef.xml");

        allFormatsRoundTripTest(ReactiveLimitsTestNetworkFactory.create(), "reactiveLimitsRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void importShouldSucceedWhenInvertedMinQMaxQ() {
        ImportOptions options = new ImportOptions()
                .setRepairInvalidReactiveCurveLimits(true);
        testForAllVersionsSince(IidmVersion.V_1_18, version -> {
            InputStream is = getVersionedNetworkAsStream("reactive-limit-inverted-minq-maxq.xiidm", version);
            Network network = NetworkSerDe.read(is, options, null);

            ReactiveCapabilityCurve curve = network.getGenerator("G1")
                    .getReactiveLimits(ReactiveCapabilityCurve.class);

            assertEquals(2.0, curve.getMinQ(100.0));
            assertEquals(10.0, curve.getMaxQ(100.0));
        });
    }

    @Test
    void importShouldThrowExceptionWhenNotInvertedMinQMaxQ() {
        ImportOptions options = new ImportOptions()
                .setRepairInvalidReactiveCurveLimits(false);
        testForAllVersionsSince(IidmVersion.V_1_18, version -> {
            InputStream is = getVersionedNetworkAsStream("reactive-limit-inverted-minq-maxq.xiidm", version);
            ValidationException e = assertThrows(
                ValidationException.class,
                () -> NetworkSerDe.read(is, options, null));

            assertTrue(e.getMessage().contains(
                "Generator 'G1': maximum reactive power is expected to be greater than or equal to minimum reactive power"));
        });
    }
}
