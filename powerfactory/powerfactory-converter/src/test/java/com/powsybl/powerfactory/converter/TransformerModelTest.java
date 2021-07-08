/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TransformerModelTest {

    @Test
    public void test() {
        TransformerModel transformerModel = TransformerModel.fromMeasures(13.806, 3.42, 0, 0, 85.5, 225);
        assertEquals(4.5143440105263144E-4, transformerModel.getR(), 0);
        assertEquals(11.285860017287098, transformerModel.getX(), 0);
        assertEquals(0, transformerModel.getG(), 0);
        assertEquals(0, transformerModel.getB(), 0);
    }
}
