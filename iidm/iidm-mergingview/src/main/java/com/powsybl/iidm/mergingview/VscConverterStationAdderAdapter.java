/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.VscConverterStationAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VscConverterStationAdderAdapter extends AbstractIdentifiableAdderAdapter<VscConverterStationAdder> implements VscConverterStationAdder {

    VscConverterStationAdderAdapter(final VscConverterStationAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public VscConverterStationAdapter add() {
        checkAndSetUniqueId();
        return getIndex().getVscConverterStation(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public VscConverterStationAdderAdapter setLossFactor(final float lossFactor) {
        getDelegate().setLossFactor(lossFactor);
        return this;
    }

    @Override
    public VscConverterStationAdderAdapter setNode(final int node) {
        getDelegate().setNode(node);
        return this;
    }

    @Override
    public VscConverterStationAdderAdapter setBus(final String bus) {
        getDelegate().setBus(bus);
        return this;
    }

    @Override
    public VscConverterStationAdderAdapter setConnectableBus(final String connectableBus) {
        getDelegate().setConnectableBus(connectableBus);
        return this;
    }

    @Override
    public VscConverterStationAdderAdapter setVoltageRegulatorOn(final boolean voltageRegulatorOn) {
        getDelegate().setVoltageRegulatorOn(voltageRegulatorOn);
        return this;
    }

    @Override
    public VscConverterStationAdderAdapter setVoltageSetpoint(final double voltageSetpoint) {
        getDelegate().setVoltageSetpoint(voltageSetpoint);
        return this;
    }

    @Override
    public VscConverterStationAdderAdapter setReactivePowerSetpoint(final double reactivePowerSetpoint) {
        getDelegate().setReactivePowerSetpoint(reactivePowerSetpoint);
        return this;
    }
}
