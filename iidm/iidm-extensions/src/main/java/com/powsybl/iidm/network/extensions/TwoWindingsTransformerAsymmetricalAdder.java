/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.math.matrix.ComplexMatrix;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TwoWindingsTransformerAsymmetricalAdder extends ExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerAsymmetrical> {

    TwoWindingsTransformerAsymmetricalAdder withOpenPhaseA1(boolean openPhaseA1);

    TwoWindingsTransformerAsymmetricalAdder withOpenPhaseB1(boolean openPhaseB1);

    TwoWindingsTransformerAsymmetricalAdder withOpenPhaseC1(boolean openPhaseC1);

    TwoWindingsTransformerAsymmetricalAdder withOpenPhaseA2(boolean openPhaseA2);

    TwoWindingsTransformerAsymmetricalAdder withOpenPhaseB2(boolean openPhaseB2);

    TwoWindingsTransformerAsymmetricalAdder withOpenPhaseC2(boolean openPhaseC2);

    TwoWindingsTransformerAsymmetricalAdder withYA(ComplexMatrix yA);

    TwoWindingsTransformerAsymmetricalAdder withYB(ComplexMatrix yB);

    TwoWindingsTransformerAsymmetricalAdder withYC(ComplexMatrix yC);
}
