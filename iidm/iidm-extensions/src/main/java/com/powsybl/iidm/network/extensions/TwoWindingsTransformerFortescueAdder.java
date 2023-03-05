/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TwoWindingsTransformerFortescueAdder extends ExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerFortescue> {

    TwoWindingsTransformerFortescueAdder withPartOfGeneratingUnit(boolean partOfGeneratingUnit);

    TwoWindingsTransformerFortescueAdder withRo(double ro);

    TwoWindingsTransformerFortescueAdder withXo(double xo);

    TwoWindingsTransformerFortescueAdder withFreeFluxes(boolean freeFluxes);

    TwoWindingsTransformerFortescueAdder withLeg1ConnectionType(LegConnectionType leg1ConnectionType);

    TwoWindingsTransformerFortescueAdder withLeg2ConnectionType(LegConnectionType leg2ConnectionType);

    TwoWindingsTransformerFortescueAdder withR1Ground(double r1Ground);

    TwoWindingsTransformerFortescueAdder withX1Ground(double x1Ground);

    TwoWindingsTransformerFortescueAdder withR2Ground(double r2Ground);

    TwoWindingsTransformerFortescueAdder withX2Ground(double x2Ground);
}
