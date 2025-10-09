/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * @author Jérémy LABOUS {@literal <jlabous at silicom.fr>}
 */
public interface ThreeWindingsTransformerPhaseAngleClockAdder
    extends ExtensionAdder<ThreeWindingsTransformer, ThreeWindingsTransformerPhaseAngleClock> {

    @Override
    default Class<ThreeWindingsTransformerPhaseAngleClock> getExtensionClass() {
        return ThreeWindingsTransformerPhaseAngleClock.class;
    }

    ThreeWindingsTransformerPhaseAngleClockAdder withPhaseAngleClockLeg2(int phaseAngleClockLeg2);

    ThreeWindingsTransformerPhaseAngleClockAdder withPhaseAngleClockLeg3(int phaseAngleClockLeg3);
}
