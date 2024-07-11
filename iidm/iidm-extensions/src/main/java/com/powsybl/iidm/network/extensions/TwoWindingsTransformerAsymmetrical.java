/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.math.matrix.ComplexMatrix;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public interface TwoWindingsTransformerAsymmetrical extends Extension<TwoWindingsTransformer> {

    String NAME = "twoWindingsTransformerAsymmetrical";

    @Override
    default String getName() {
        return NAME;
    }

    boolean isOpenPhaseA1();

    void setOpenPhaseA1(boolean openPhaseA1);

    boolean isOpenPhaseB1();

    void setOpenPhaseB1(boolean openPhaseB1);

    boolean isOpenPhaseC1();

    void setOpenPhaseC1(boolean openPhaseC1);

    boolean isOpenPhaseA2();

    void setOpenPhaseA2(boolean openPhaseA2);

    boolean isOpenPhaseB2();

    void setOpenPhaseB2(boolean openPhaseB2);

    boolean isOpenPhaseC2();

    void setOpenPhaseC2(boolean openPhaseC2);

    /**
     * Get admittance complex matrix for phase A.
     */
    ComplexMatrix getAdmittanceMatrixA();

    void setAdmittanceMatrixA(ComplexMatrix admittanceMatrixA);

    /**
     * Get admittance complex matrix for phase B.
     */
    ComplexMatrix getAdmittanceMatrixB();

    void setAdmittanceMatrixB(ComplexMatrix admittanceMatrixB);

    /**
     * Get admittance complex matrix for phase C.
     */
    ComplexMatrix getAdmittanceMatrixC();

    void setAdmittanceMatrixC(ComplexMatrix admittanceMatrixC);
}
