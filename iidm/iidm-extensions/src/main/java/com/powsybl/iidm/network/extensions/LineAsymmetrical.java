/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Line;
import com.powsybl.math.matrix.ComplexMatrix;

/**
 * An asymmetrical line is modeled by:
 * - the connection status of each phase A, B and C.
 * - its physical characteristics Rz, Xz, Rn and Zn (knowing that Rp and Rp, balanced characteristics, are present in
 * the line attributes). From these values, we compute the Fortescue admittance matrix admittanceMatrixABC for computation engine.
 *
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public interface LineAsymmetrical extends Extension<Line> {

    String NAME = "lineAsymmetrical";

    @Override
    default String getName() {
        return NAME;
    }

    boolean isOpenPhaseA();

    void setOpenPhaseA(boolean openPhaseA);

    boolean isOpenPhaseB();

    void setOpenPhaseB(boolean openPhaseB);

    boolean isOpenPhaseC();

    void setOpenPhaseC(boolean openPhaseC);

    ComplexMatrix getAdmittanceMatrixABC();

    void setAdmittanceMatrixABC(ComplexMatrix admittanceMatrixABC);
}
