/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.test.ReactiveLimitsTestNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void maxQInferiorToMinQExceptionTest() {
        // Test for the last version with no check in minQ/maxQ (IIDM v1.13)
        try (InputStream is = getVersionedNetworkAsStream("reactiveLimitsMinMaxException.xml", IidmVersion.V_1_13)) {
            assertDoesNotThrow(() -> NetworkSerDe.read(is));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Test for the following versions with check in minQ/maxQ (starting at IIDM v1.14)
        testForAllVersionsSince(IidmVersion.V_1_14, version -> {
            try (InputStream is = getVersionedNetworkAsStream("reactiveLimitsMinMaxException.xml", version)) {
                ValidationException e = assertThrows(ValidationException.class, () -> NetworkSerDe.read(is));
                assertTrue(e.getMessage().contains("maximum reactive power is expected to be greater than or equal to minimum reactive power"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
