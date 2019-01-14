/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.List;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableVscConverterStation extends AbstractImmutableIdentifiable<VscConverterStation> implements VscConverterStation {

    @Override
    public boolean isVoltageRegulatorOn() {
        return identifiable.isVoltageRegulatorOn();
    }

    @Override
    public HvdcConverterStation setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getVoltageSetpoint() {
        return identifiable.getVoltageSetpoint();
    }

    @Override
    public HvdcConverterStation setVoltageSetpoint(double voltageSetpoint) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getReactivePowerSetpoint() {
        return identifiable.getReactivePowerSetpoint();
    }

    @Override
    public HvdcConverterStation setReactivePowerSetpoint(double reactivePowerSetpoint) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public HvdcType getHvdcType() {
        return identifiable.getHvdcType();
    }

    @Override
    public float getLossFactor() {
        return identifiable.getLossFactor();
    }

    @Override
    public VscConverterStation setLossFactor(float lossFactor) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Terminal getTerminal() {
        return identifiable.getTerminal();
    }

    @Override
    public ConnectableType getType() {
        return identifiable.getType();
    }

    @Override
    public List<? extends Terminal> getTerminals() {
//        return identifiable.getTerminals().stream().map();
        return null;
    }

    @Override
    public void remove() {

    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        return null;
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        return null;
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return null;
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return null;
    }

    protected ImmutableVscConverterStation(VscConverterStation identifiable) {
        super(identifiable);
    }
}
