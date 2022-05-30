/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.GeneratorStartupAdder;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class GeneratorStartupAdderImpl extends AbstractExtensionAdder<Generator, GeneratorStartup> implements GeneratorStartupAdder {

    private float predefinedActivePowerSetpoint = Float.NaN;

    private float startUpCost = Float.NaN;

    private float marginalCost = Float.NaN;

    private float plannedOutageRate = Float.NaN;

    private float forcedOutageRate = Float.NaN;

    public GeneratorStartupAdderImpl(Generator generator) {
        super(generator);
    }

    @Override
    protected GeneratorStartup createExtension(Generator extendable) {
        return new GeneratorStartupImpl(extendable, predefinedActivePowerSetpoint, startUpCost, marginalCost, plannedOutageRate, forcedOutageRate);
    }

    @Override
    public GeneratorStartupAdderImpl withPredefinedActivePowerSetpoint(float predefinedActivePowerSetpoint) {
        this.predefinedActivePowerSetpoint = predefinedActivePowerSetpoint;
        return this;
    }

    @Override
    public GeneratorStartupAdder withStartUpCost(float startUpCost) {
        this.startUpCost = startUpCost;
        return this;
    }

    @Override
    public GeneratorStartupAdderImpl withMarginalCost(float marginalCost) {
        this.marginalCost = marginalCost;
        return this;
    }

    @Override
    public GeneratorStartupAdderImpl withPlannedOutageRate(float plannedOutageRate) {
        this.plannedOutageRate = plannedOutageRate;
        return this;
    }

    @Override
    public GeneratorStartupAdderImpl withForcedOutageRate(float forcedOutageRate) {
        this.forcedOutageRate = forcedOutageRate;
        return this;
    }
}
