/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ThreeWindingsTransformerFortescueAdder extends ExtensionAdder<ThreeWindingsTransformer, ThreeWindingsTransformerFortescue> {

    ThreeWindingsTransformerFortescueAdder withLeg1Ro(double leg1Ro);

    ThreeWindingsTransformerFortescueAdder withLeg2Ro(double leg2Ro);

    ThreeWindingsTransformerFortescueAdder withLeg3Ro(double leg3Ro);

    ThreeWindingsTransformerFortescueAdder withLeg1Xo(double leg1Xo);

    ThreeWindingsTransformerFortescueAdder withLeg2Xo(double leg2Xo);

    ThreeWindingsTransformerFortescueAdder withLeg3Xo(double leg3Xo);

    ThreeWindingsTransformerFortescueAdder withLeg1FreeFluxes(boolean leg1FreeFluxes);

    ThreeWindingsTransformerFortescueAdder withLeg2FreeFluxes(boolean leg2FreeFluxes);

    ThreeWindingsTransformerFortescueAdder withLeg3FreeFluxes(boolean leg3FreeFluxes);

    ThreeWindingsTransformerFortescueAdder withLeg1ConnectionType(LegConnectionType leg1ConnectionType);

    ThreeWindingsTransformerFortescueAdder withLeg2ConnectionType(LegConnectionType leg2ConnectionType);

    ThreeWindingsTransformerFortescueAdder withLeg3ConnectionType(LegConnectionType leg3ConnectionType);
}
