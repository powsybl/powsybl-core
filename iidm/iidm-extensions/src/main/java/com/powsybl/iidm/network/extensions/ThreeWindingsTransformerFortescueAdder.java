/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ThreeWindingsTransformerFortescueAdder extends ExtensionAdder<ThreeWindingsTransformer, ThreeWindingsTransformerFortescue> {

    interface LegFortescueAdder {

        LegFortescueAdder withRz(double rz);

        LegFortescueAdder withXz(double xz);

        LegFortescueAdder withFreeFluxes(boolean freeFluxes);

        LegFortescueAdder withConnectionType(WindingConnectionType connectionType);

        LegFortescueAdder withGroundingR(double groundingR);

        LegFortescueAdder withGroundingX(double groundingX);

        LegFortescueAdder leg1();

        LegFortescueAdder leg2();

        LegFortescueAdder leg3();

        ThreeWindingsTransformerFortescue add();
    }

    LegFortescueAdder leg1();

    LegFortescueAdder leg2();

    LegFortescueAdder leg3();
}
