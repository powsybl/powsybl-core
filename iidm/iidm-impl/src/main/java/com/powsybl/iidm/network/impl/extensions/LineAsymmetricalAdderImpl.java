/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineAsymmetrical;
import com.powsybl.iidm.network.extensions.LineAsymmetricalAdder;
import com.powsybl.math.matrix.ComplexMatrix;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public class LineAsymmetricalAdderImpl extends AbstractExtensionAdder<Line, LineAsymmetrical> implements LineAsymmetricalAdder {

    private boolean openPhaseA = false;
    private boolean openPhaseB = false;
    private boolean openPhaseC = false;

    private ComplexMatrix admittanceMatrixABC = null;

    public LineAsymmetricalAdderImpl(Line line) {
        super(line);
    }

    @Override
    public Class<? super LineAsymmetrical> getExtensionClass() {
        return LineAsymmetrical.class;
    }

    @Override
    protected LineAsymmetricalImpl createExtension(Line line) {
        return new LineAsymmetricalImpl(line, openPhaseA, openPhaseB, openPhaseC, admittanceMatrixABC);
    }

    @Override
    public LineAsymmetricalAdder withOpenPhaseA(boolean openPhaseA) {
        this.openPhaseA = openPhaseA;
        return this;
    }

    @Override
    public LineAsymmetricalAdder withOpenPhaseB(boolean openPhaseB) {
        this.openPhaseB = openPhaseB;
        return this;
    }

    @Override
    public LineAsymmetricalAdder withOpenPhaseC(boolean openPhaseC) {
        this.openPhaseC = openPhaseC;
        return this;
    }
}
