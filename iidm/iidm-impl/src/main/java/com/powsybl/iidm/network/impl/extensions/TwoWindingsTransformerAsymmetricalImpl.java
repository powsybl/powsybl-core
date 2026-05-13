/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerAsymmetrical;
import com.powsybl.math.matrix.ComplexMatrix;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public class TwoWindingsTransformerAsymmetricalImpl extends AbstractExtension<TwoWindingsTransformer> implements TwoWindingsTransformerAsymmetrical {

    private boolean openPhaseA1;
    private boolean openPhaseB1;
    private boolean openPhaseC1;
    private boolean openPhaseA2;
    private boolean openPhaseB2;
    private boolean openPhaseC2;
    private ComplexMatrix admittanceMatrixA;
    private ComplexMatrix admittanceMatrixB;
    private ComplexMatrix admittanceMatrixC;

    public TwoWindingsTransformerAsymmetricalImpl(TwoWindingsTransformer transformer,
                                                  boolean openPhaseA1, boolean openPhaseB1, boolean openPhaseC1,
                                                  boolean openPhaseA2, boolean openPhaseB2, boolean openPhaseC2,
                                                  ComplexMatrix admittanceMatrixA, ComplexMatrix admittanceMatrixB, ComplexMatrix admittanceMatrixC) {
        super(transformer);
        this.openPhaseA1 = openPhaseA1;
        this.openPhaseB1 = openPhaseB1;
        this.openPhaseC1 = openPhaseC1;
        this.openPhaseA2 = openPhaseA2;
        this.openPhaseB2 = openPhaseB2;
        this.openPhaseC2 = openPhaseC2;
        this.admittanceMatrixA = admittanceMatrixA;
        this.admittanceMatrixB = admittanceMatrixB;
        this.admittanceMatrixC = admittanceMatrixC;
    }

    @Override
    public boolean isOpenPhaseA1() {
        return openPhaseA1;
    }

    @Override
    public void setOpenPhaseA1(boolean openPhaseA1) {
        this.openPhaseA1 = openPhaseA1;
    }

    @Override
    public boolean isOpenPhaseB1() {
        return openPhaseB1;
    }

    @Override
    public void setOpenPhaseB1(boolean openPhaseB1) {
        this.openPhaseB1 = openPhaseB1;
    }

    @Override
    public boolean isOpenPhaseC1() {
        return openPhaseC1;
    }

    @Override
    public void setOpenPhaseC1(boolean openPhaseC1) {
        this.openPhaseC1 = openPhaseC1;
    }

    @Override
    public boolean isOpenPhaseA2() {
        return openPhaseA2;
    }

    @Override
    public void setOpenPhaseA2(boolean openPhaseA2) {
        this.openPhaseA2 = openPhaseA2;
    }

    @Override
    public boolean isOpenPhaseB2() {
        return openPhaseB2;
    }

    @Override
    public void setOpenPhaseB2(boolean openPhaseB2) {
        this.openPhaseB2 = openPhaseB2;
    }

    @Override
    public boolean isOpenPhaseC2() {
        return openPhaseC2;
    }

    @Override
    public void setOpenPhaseC2(boolean openPhaseC2) {
        this.openPhaseC2 = openPhaseC2;
    }

    @Override
    public ComplexMatrix getAdmittanceMatrixA() {
        return admittanceMatrixA;
    }

    @Override
    public void setAdmittanceMatrixA(ComplexMatrix admittanceMatrixA) {
        this.admittanceMatrixA = admittanceMatrixA;
    }

    @Override
    public ComplexMatrix getAdmittanceMatrixB() {
        return admittanceMatrixB;
    }

    @Override
    public void setAdmittanceMatrixB(ComplexMatrix admittanceMatrixB) {
        this.admittanceMatrixB = admittanceMatrixB;
    }

    @Override
    public ComplexMatrix getAdmittanceMatrixC() {
        return admittanceMatrixC;
    }

    @Override
    public void setAdmittanceMatrixC(ComplexMatrix admittanceMatrixC) {
        this.admittanceMatrixC = admittanceMatrixC;
    }
}
