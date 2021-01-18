/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class ActivePowerControlImpl<T extends Injection<T>> extends AbstractExtension<T>
        implements ActivePowerControl<T> {

    private boolean participate;

    private float droop;

    public ActivePowerControlImpl(T component, boolean participate, float droop) {
        super(component);
        this.participate = participate;
        this.droop = droop;
    }

    public boolean isParticipate() {
        return participate;
    }

    public void setParticipate(boolean participate) {
        this.participate = participate;
    }

    public float getDroop() {
        return droop;
    }

    public void setDroop(float droop) {
        this.droop = droop;
    }
}
