/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Injection;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class ActivePowerControl<T extends Injection> extends AbstractExtension<T> {

    private boolean participate;

    private float droop;

    public ActivePowerControl(Generator generator, boolean participate, float droop) {
        this((T) generator, participate, droop);
    }

    public ActivePowerControl(Battery battery, boolean participate, float droop) {
        this((T) battery, participate, droop);
    }

    ActivePowerControl(T component, boolean participate, float droop) {
        super(component);
        this.participate = participate;
        this.droop = droop;
    }

    @Override
    public String getName() {
        return "activePowerControl";
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
