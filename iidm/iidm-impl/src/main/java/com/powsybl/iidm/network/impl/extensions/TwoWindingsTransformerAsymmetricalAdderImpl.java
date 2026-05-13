/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerAsymmetrical;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerAsymmetricalAdder;
import com.powsybl.math.matrix.ComplexMatrix;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public class TwoWindingsTransformerAsymmetricalAdderImpl extends
        AbstractExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerAsymmetrical> implements TwoWindingsTransformerAsymmetricalAdder {

    private boolean openPhaseA1 = false;
    private boolean openPhaseB1 = false;
    private boolean openPhaseC1 = false;
    private boolean openPhaseA2 = false;
    private boolean openPhaseB2 = false;
    private boolean openPhaseC2 = false;
    private ComplexMatrix admittanceMatrixA;
    private ComplexMatrix admittanceMatrixB;
    private ComplexMatrix admittanceMatrixC;

    public TwoWindingsTransformerAsymmetricalAdderImpl(TwoWindingsTransformer transformer) {
        super(transformer);
    }

    @Override
    public Class<? super TwoWindingsTransformerAsymmetrical> getExtensionClass() {
        return TwoWindingsTransformerAsymmetrical.class;
    }

    @Override
    protected TwoWindingsTransformerAsymmetricalImpl createExtension(TwoWindingsTransformer transformer) {
        return new TwoWindingsTransformerAsymmetricalImpl(transformer, openPhaseA1, openPhaseB1, openPhaseC1,
                                                          openPhaseA2, openPhaseB2, openPhaseC2,
                                                          admittanceMatrixA, admittanceMatrixB, admittanceMatrixC);
    }

    @Override
    public TwoWindingsTransformerAsymmetricalAdder withOpenPhaseA1(boolean openPhaseA1) {
        this.openPhaseA1 = openPhaseA1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAsymmetricalAdder withOpenPhaseB1(boolean openPhaseB1) {
        this.openPhaseB1 = openPhaseB1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAsymmetricalAdder withOpenPhaseC1(boolean openPhaseC1) {
        this.openPhaseC1 = openPhaseC1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAsymmetricalAdder withOpenPhaseA2(boolean openPhaseA2) {
        this.openPhaseA2 = openPhaseA2;
        return this;
    }

    @Override
    public TwoWindingsTransformerAsymmetricalAdder withOpenPhaseB2(boolean openPhaseB2) {
        this.openPhaseB2 = openPhaseB2;
        return this;
    }

    @Override
    public TwoWindingsTransformerAsymmetricalAdder withOpenPhaseC2(boolean openPhaseC2) {
        this.openPhaseC2 = openPhaseC2;
        return this;
    }

    @Override
    public TwoWindingsTransformerAsymmetricalAdder withAdmittanceMatrixA(ComplexMatrix yA) {
        this.admittanceMatrixA = yA;
        return this;
    }

    @Override
    public TwoWindingsTransformerAsymmetricalAdder withAdmittanceMatrixB(ComplexMatrix yB) {
        this.admittanceMatrixB = yB;
        return this;
    }

    @Override
    public TwoWindingsTransformerAsymmetricalAdder withAdmittanceMatrixC(ComplexMatrix yC) {
        this.admittanceMatrixC = yC;
        return this;
    }
}
