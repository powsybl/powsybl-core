/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import org.junit.jupiter.api.Test;

import com.powsybl.powerfactory.converter.TransformerConverter.TransformerModel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.complex.Complex;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TransformerModelTest {

    @Test
    void test() {
        Complex impedance = TransformerModel.createImpedanceFromMeasures(13.806, 3.42, 85.5, 225);
        Complex shuntAdmittance = TransformerModel.createShuntAdmittanceFromMeasures(0, 0, 85.5, 225);
        assertEquals(0.023684210526315787, impedance.getReal(), 0);
        assertEquals(81.74604920057632, impedance.getImaginary(), 0);
        assertEquals(0, shuntAdmittance.getReal(), 0);
        assertEquals(0, shuntAdmittance.getImaginary(), 0);
    }
}
