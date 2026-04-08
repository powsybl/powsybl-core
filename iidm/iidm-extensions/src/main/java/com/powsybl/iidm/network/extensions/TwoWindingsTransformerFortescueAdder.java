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

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TwoWindingsTransformerFortescueAdder extends ExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerFortescue> {

    TwoWindingsTransformerFortescueAdder withRz(double rz);

    TwoWindingsTransformerFortescueAdder withXz(double xz);

    TwoWindingsTransformerFortescueAdder withFreeFluxes(boolean freeFluxes);

    TwoWindingsTransformerFortescueAdder withConnectionType1(WindingConnectionType connectionType1);

    TwoWindingsTransformerFortescueAdder withConnectionType2(WindingConnectionType connectionType2);

    TwoWindingsTransformerFortescueAdder withGroundingR1(double r1Ground);

    TwoWindingsTransformerFortescueAdder withGroundingX1(double x1Ground);

    TwoWindingsTransformerFortescueAdder withGroundingR2(double r2Ground);

    TwoWindingsTransformerFortescueAdder withGroundingX2(double x2Ground);
}
