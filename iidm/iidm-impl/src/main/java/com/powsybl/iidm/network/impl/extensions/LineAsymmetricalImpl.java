/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineAsymmetrical;
import com.powsybl.math.matrix.ComplexMatrix;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public class LineAsymmetricalImpl extends AbstractExtension<Line> implements LineAsymmetrical {

    private boolean openPhaseA;
    private boolean openPhaseB;
    private boolean openPhaseC;

    private ComplexMatrix admittanceMatrixABC;

    public LineAsymmetricalImpl(Line line, boolean openPhaseA, boolean openPhaseB, boolean openPhaseC,
                                ComplexMatrix admittanceMatrixABC) {
        super(line);
        this.openPhaseA = openPhaseA;
        this.openPhaseB = openPhaseB;
        this.openPhaseC = openPhaseC;
        this.admittanceMatrixABC = admittanceMatrixABC;
    }

    @Override
    public boolean isOpenPhaseA() {
        return openPhaseA;
    }

    @Override
    public void setOpenPhaseA(boolean openPhaseA) {
        this.openPhaseA = openPhaseA;
    }

    @Override
    public boolean isOpenPhaseB() {
        return openPhaseB;
    }

    @Override
    public void setOpenPhaseB(boolean openPhaseB) {
        this.openPhaseB = openPhaseB;
    }

    @Override
    public boolean isOpenPhaseC() {
        return openPhaseC;
    }

    @Override
    public void setOpenPhaseC(boolean openPhaseC) {
        this.openPhaseC = openPhaseC;
    }

    @Override
    public ComplexMatrix getAdmittanceMatrixABC() {
        return admittanceMatrixABC;
    }

    @Override
    public void setAdmittanceMatrixABC(ComplexMatrix admittanceMatrixABC) {
        this.admittanceMatrixABC = admittanceMatrixABC;
    }
}
