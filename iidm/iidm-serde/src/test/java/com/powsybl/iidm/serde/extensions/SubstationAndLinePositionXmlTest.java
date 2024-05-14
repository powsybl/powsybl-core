/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SubstationAndLinePositionXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2022-04-06T13:43:05.020+02:00"));

        // create substation and line position extensions
        network.getSubstation("P1").newExtension(SubstationPositionAdder.class)
                .withCoordinate(new Coordinate(48, 2))
                .add();
        network.getLine("NHV1_NHV2_1").newExtension(LinePositionAdder.class)
                .withCoordinates(List.of(new Coordinate(48, 2), new Coordinate(48.1, 2.1)))
                .add();

        Network network2 = allFormatsRoundTripTest(network, "/substationAndLinePositionRoundTripRef.xml", CURRENT_IIDM_VERSION);

        var substationPosition = network2.getSubstation("P1").getExtension(SubstationPosition.class);
        assertNotNull(substationPosition);
        var linePosition = network2.getLine("NHV1_NHV2_1").getExtension(LinePosition.class);
        assertNotNull(linePosition);
    }
}
