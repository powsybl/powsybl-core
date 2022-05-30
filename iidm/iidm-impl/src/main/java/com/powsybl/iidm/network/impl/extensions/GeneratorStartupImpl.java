/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class GeneratorStartupImpl extends AbstractExtension<Generator> implements GeneratorStartup {

    private float predefinedActivePowerSetpoint;

    private float startUpCost;

    private float marginalCost;

    private float plannedOutageRate;

    private float forcedOutageRate;

    public GeneratorStartupImpl(Generator generator, float predefinedActivePowerSetpoint, float startUpCost, float marginalCost, float plannedOutageRate, float forcedOutageRate) {
        super(generator);
        this.predefinedActivePowerSetpoint = predefinedActivePowerSetpoint;
        this.startUpCost = startUpCost;
        this.marginalCost = marginalCost;
        this.plannedOutageRate = plannedOutageRate;
        this.forcedOutageRate = forcedOutageRate;
    }

    @Override
    public float getPredefinedActivePowerSetpoint() {
        return predefinedActivePowerSetpoint;
    }

    @Override
    public GeneratorStartupImpl setPredefinedActivePowerSetpoint(float predefinedActivePowerSetpoint) {
        this.predefinedActivePowerSetpoint = predefinedActivePowerSetpoint;
        return this;
    }

    @Override
    public float getStartUpCost() {
        return startUpCost;
    }

    @Override
    public GeneratorStartup setStartUpCost(float startUpCost) {
        this.startUpCost = startUpCost;
        return this;
    }

    @Override
    public float getMarginalCost() {
        return marginalCost;
    }

    @Override
    public GeneratorStartupImpl setMarginalCost(float marginalCost) {
        this.marginalCost = marginalCost;
        return this;
    }

    @Override
    public float getPlannedOutageRate() {
        return plannedOutageRate;
    }

    @Override
    public GeneratorStartupImpl setPlannedOutageRate(float plannedOutageRate) {
        this.plannedOutageRate = plannedOutageRate;
        return this;
    }

    @Override
    public float getForcedOutageRate() {
        return forcedOutageRate;
    }

    @Override
    public GeneratorStartupImpl setForcedOutageRate(float forcedOutageRate) {
        this.forcedOutageRate = forcedOutageRate;
        return this;
    }
}
