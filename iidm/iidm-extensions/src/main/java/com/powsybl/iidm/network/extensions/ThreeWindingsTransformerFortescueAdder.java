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

    ThreeWindingsTransformerFortescueAdder withLeg1R0(double leg1R0);

    ThreeWindingsTransformerFortescueAdder withLeg2R0(double leg2R0);

    ThreeWindingsTransformerFortescueAdder withLeg3R0(double leg3R0);

    ThreeWindingsTransformerFortescueAdder withLeg1X0(double leg1X0);

    ThreeWindingsTransformerFortescueAdder withLeg2X0(double leg2X0);

    ThreeWindingsTransformerFortescueAdder withLeg3X0(double leg3X0);

    ThreeWindingsTransformerFortescueAdder withLeg1FreeFluxes(boolean leg1FreeFluxes);

    ThreeWindingsTransformerFortescueAdder withLeg2FreeFluxes(boolean leg2FreeFluxes);

    ThreeWindingsTransformerFortescueAdder withLeg3FreeFluxes(boolean leg3FreeFluxes);

    ThreeWindingsTransformerFortescueAdder withLeg1ConnectionType(WindingConnectionType leg1ConnectionType);

    ThreeWindingsTransformerFortescueAdder withLeg2ConnectionType(WindingConnectionType leg2ConnectionType);

    ThreeWindingsTransformerFortescueAdder withLeg3ConnectionType(WindingConnectionType leg3ConnectionType);

    ThreeWindingsTransformerFortescueAdder withLeg1GroundingR(double leg1GroundingR);

    ThreeWindingsTransformerFortescueAdder withLeg1GroundingX(double leg1GroundingX);

    ThreeWindingsTransformerFortescueAdder withLeg2GroundingR(double leg2GroundingR);

    ThreeWindingsTransformerFortescueAdder withLeg2GroundingX(double leg2GroundingX);

    ThreeWindingsTransformerFortescueAdder withLeg3GroundingR(double leg3GroundingR);

    ThreeWindingsTransformerFortescueAdder withLeg3GroundingX(double leg3GroundingX);
}
