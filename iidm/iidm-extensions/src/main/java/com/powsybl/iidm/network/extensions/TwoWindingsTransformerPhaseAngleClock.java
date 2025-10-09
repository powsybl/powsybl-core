/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Jérémy LABOUS {@literal <jlabous at silicom.fr>}
 */
public interface TwoWindingsTransformerPhaseAngleClock extends Extension<TwoWindingsTransformer> {

    String NAME = "twoWindingsTransformerPhaseAngleClock";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Get the phase angle displacement represented in clock hours.
     */
    int getPhaseAngleClock();

    /**
     * Set the phase angle displacement represented in clock hours
     */
    void setPhaseAngleClock(int phaseAngleClock);
}
