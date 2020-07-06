/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Load;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class LoadDetailImpl extends AbstractExtension<Load> implements LoadDetail {

    private float fixedActivePower;

    private float fixedReactivePower;

    private float variableActivePower;

    private float variableReactivePower;

    public LoadDetailImpl(Load load, float fixedActivePower, float fixedReactivePower,
                          float variableActivePower, float variableReactivePower) {
        super(load);
        this.fixedActivePower = checkPower(fixedActivePower, "Invalid fixedActivePower");
        this.fixedReactivePower = checkPower(fixedReactivePower, "Invalid fixedReactivePower");
        this.variableActivePower = checkPower(variableActivePower, "Invalid variableActivePower");
        this.variableReactivePower = checkPower(variableReactivePower, "Invalid variableReactivePower");
    }

    public float getFixedActivePower() {
        return fixedActivePower;
    }

    @Override
    public LoadDetail setFixedActivePower(float fixedActivePower) {
        this.fixedActivePower = checkPower(fixedActivePower, "Invalid fixedActivePower");
        return this;
    }

    @Override
    public float getFixedReactivePower() {
        return fixedReactivePower;
    }

    @Override
    public LoadDetail setFixedReactivePower(float fixedReactivePower) {
        this.fixedReactivePower = checkPower(fixedReactivePower, "Invalid fixedReactivePower");
        return this;
    }

    @Override
    public float getVariableActivePower() {
        return variableActivePower;
    }

    @Override
    public LoadDetail setVariableActivePower(float variableActivePower) {
        this.variableActivePower = checkPower(variableActivePower, "Invalid variableActivePower");
        return this;
    }

    @Override
    public float getVariableReactivePower() {
        return variableReactivePower;
    }

    @Override
    public LoadDetail setVariableReactivePower(float variableReactivePower) {
        this.variableReactivePower = checkPower(variableReactivePower, "Invalid variableReactivePower");
        return this;
    }

    private static float checkPower(float power, String errorMessage) {
        if (Float.isNaN(power)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return power;
    }
}
