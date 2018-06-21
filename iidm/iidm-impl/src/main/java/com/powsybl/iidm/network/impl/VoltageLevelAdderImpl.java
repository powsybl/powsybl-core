/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevelAdder;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VoltageLevelAdderImpl extends AbstractIdentifiableAdder<VoltageLevelAdderImpl> implements VoltageLevelAdder {

    private final SubstationImpl substation;

    private double nominalV = Double.NaN;

    private double lowVoltageLimit = Double.NaN;

    private double highVoltageLimit = Double.NaN;

    private TopologyKind topologyKind;

    VoltageLevelAdderImpl(SubstationImpl substation) {
        this.substation = substation;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return substation.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "Voltage level";
    }

    @Override
    public VoltageLevelAdder setNominalV(double nominalV) {
        this.nominalV = nominalV;
        return this;
    }

    @Override
    public VoltageLevelAdder setLowVoltageLimit(double lowVoltageLimit) {
        this.lowVoltageLimit = lowVoltageLimit;
        return this;
    }

    @Override
    public VoltageLevelAdder setHighVoltageLimit(double highVoltageLimit) {
        this.highVoltageLimit = highVoltageLimit;
        return this;
    }

    @Override
    public VoltageLevelAdder setTopologyKind(String topologyKind) {
        this.topologyKind = TopologyKind.valueOf(topologyKind);
        return this;
    }

    @Override
    public VoltageLevelAdder setTopologyKind(TopologyKind topologyKind) {
        this.topologyKind = topologyKind;
        return this;
    }

    @Override
    public VoltageLevel add() {
        String id = checkAndGetUniqueId();
        // TODO : ckeck that there are not another voltage level with same base voltage

        ValidationUtil.checkNominalV(this, nominalV);
        ValidationUtil.checkVoltageLimits(this, lowVoltageLimit, highVoltageLimit);
        ValidationUtil.checkTopologyKind(this, topologyKind);

        VoltageLevelExt voltageLevel;
        switch (topologyKind) {
            case NODE_BREAKER:
                voltageLevel = new NodeBreakerVoltageLevel(id, getName(), substation, nominalV, lowVoltageLimit, highVoltageLimit);
                break;
            case BUS_BREAKER:
                voltageLevel = new BusBreakerVoltageLevel(id, getName(), substation, nominalV, lowVoltageLimit, highVoltageLimit);
                break;
            default:
                throw new AssertionError();
        }
        getNetwork().getObjectStore().checkAndAdd(voltageLevel);
        substation.addVoltageLevel(voltageLevel);
        getNetwork().getListeners().notifyCreation(voltageLevel);
        return voltageLevel;
    }

}
