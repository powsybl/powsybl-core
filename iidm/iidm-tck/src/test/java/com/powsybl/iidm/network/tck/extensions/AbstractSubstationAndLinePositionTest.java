/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractSubstationAndLinePositionTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        var p1 = network.getSubstation("P1");
        var substationPosition = p1.newExtension(SubstationPositionAdder.class)
                .withCoordinate(new Coordinate(48, 2))
                .add();
        assertNotNull(substationPosition);
        assertEquals(new Coordinate(48, 2), substationPosition.getCoordinate());

        var l1 = network.getLine("NHV1_NHV2_1");
        l1.newExtension(LinePositionAdder.class)
                .withCoordinates(List.of(new Coordinate(48, 2), new Coordinate(48.1, 2.1)))
                .add();
        var linePosition = l1.getExtension(LinePosition.class);
        assertNotNull(linePosition);
        assertEquals(List.of(new Coordinate(48, 2), new Coordinate(48.1, 2.1)), linePosition.getCoordinates());
    }
}
