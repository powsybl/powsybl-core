/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractFortescueExtensionTest {

    @Test
    void testGenerator() {
        var network = EurostagTutorialExample1Factory.create();
        var gen = network.getGenerator("GEN");
        GeneratorFortescue fortescue = gen.newExtension(GeneratorFortescueAdder.class)
                .withGeneratorType(GeneratorFortescue.GeneratorType.ROTATING_MACHINE)
                .withRo(0.1d)
                .withXo(2d)
                .withRi(0.2d)
                .withXi(2.4d)
                .withToGround(true)
                .withGroundingR(0.02d)
                .withGroundingX(0.3d)
                .add();
        assertSame(GeneratorFortescue.GeneratorType.ROTATING_MACHINE, fortescue.getGeneratorType());
    }
}
