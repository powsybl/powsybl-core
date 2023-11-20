/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer.extensions;

import com.powsybl.commons.test.AbstractSerializerTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serializer.NetworkSerializer;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.powsybl.iidm.serializer.AbstractIidmSerializerTest.getVersionedNetworkPath;
import static com.powsybl.iidm.serializer.IidmSerializerConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SubstationAndLinePositionXmlTest extends AbstractSerializerTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2022-04-06T13:43:05.020+02:00"));

        // create substation and line position extensions
        network.getSubstation("P1").newExtension(SubstationPositionAdder.class)
                .withCoordinate(new Coordinate(48, 2))
                .add();
        network.getLine("NHV1_NHV2_1").newExtension(LinePositionAdder.class)
                .withCoordinates(List.of(new Coordinate(48, 2), new Coordinate(48.1, 2.1)))
                .add();

        Network network2 = roundTripXmlTest(network,
                NetworkSerializer::writeAndValidate,
                NetworkSerializer::read,
                getVersionedNetworkPath("/substationAndLinePositionRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));

        var substationPosition = network2.getSubstation("P1").getExtension(SubstationPosition.class);
        assertNotNull(substationPosition);
        var linePosition = network2.getLine("NHV1_NHV2_1").getExtension(LinePosition.class);
        assertNotNull(linePosition);
    }
}
