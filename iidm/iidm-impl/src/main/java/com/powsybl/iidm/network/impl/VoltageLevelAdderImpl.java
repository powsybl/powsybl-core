/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevelAdder;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VoltageLevelAdderImpl extends AbstractIdentifiableAdder<VoltageLevelAdderImpl> implements VoltageLevelAdder {

    private final Ref<NetworkImpl> networkRef;
    private final SubstationImpl substation;

    private double nominalV = Double.NaN;

    private double lowVoltageLimit = Double.NaN;

    private double highVoltageLimit = Double.NaN;

    private TopologyKind topologyKind;

    VoltageLevelAdderImpl(SubstationImpl substation) {
        networkRef = null;
        this.substation = substation;
    }

    VoltageLevelAdderImpl(Ref<NetworkImpl> networkRef) {
        this.networkRef = networkRef;
        substation = null;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return Optional.ofNullable(networkRef)
                .map(Ref::get)
                .orElseGet(() -> Optional.ofNullable(substation)
                        .map(SubstationImpl::getNetwork)
                        .orElseThrow(() -> new PowsyblException("Voltage level has no container")));
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
                voltageLevel = new NodeBreakerVoltageLevel(id, getName(), isFictitious(), substation, networkRef, nominalV, lowVoltageLimit, highVoltageLimit);
                break;
            case BUS_BREAKER:
                voltageLevel = new BusBreakerVoltageLevel(id, getName(), isFictitious(), substation, networkRef, nominalV, lowVoltageLimit, highVoltageLimit);
                break;
            default:
                throw new AssertionError();
        }
        getNetwork().getIndex().checkAndAdd(voltageLevel);
        Optional.ofNullable(substation).ifPresent(s -> s.addVoltageLevel(voltageLevel));
        getNetwork().getListeners().notifyCreation(voltageLevel);
        return voltageLevel;
    }

}
