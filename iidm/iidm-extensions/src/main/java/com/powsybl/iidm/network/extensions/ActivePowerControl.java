/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public interface ActivePowerControl<I extends Injection<I>> extends Extension<I> {

    String NAME = "activePowerControl";

    @Override
    default String getName() {
        return NAME;
    }

    boolean isParticipate();

    void setParticipate(boolean participate);

    float getDroop();

    void setDroop(float droop);

}
