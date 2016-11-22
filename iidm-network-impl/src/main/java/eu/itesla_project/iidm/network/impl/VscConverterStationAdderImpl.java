/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.VscConverterStationAdder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class VscConverterStationAdderImpl extends SingleTerminalConnectableAdderImpl<VscConverterStationAdderImpl> implements VscConverterStationAdder {

    private final VoltageLevelExt voltageLevel;

    private Boolean voltageRegulatorOn;

    private float reactivePowerSetPoint = Float.NaN;

    private float voltageSetPoint = Float.NaN;

    VscConverterStationAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return VscConverterStationImpl.TYPE_DESCRIPTION;
    }

    @Override
    public VscConverterStationAdderImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public VscConverterStationAdderImpl setVoltageSetPoint(float voltageSetPoint) {
        this.voltageSetPoint = voltageSetPoint;
        return this;
    }

    @Override
    public VscConverterStationAdderImpl setReactivePowerSetPoint(float reactivePowerSetPoint) {
        this.reactivePowerSetPoint = reactivePowerSetPoint;
        return this;
    }

    @Override
    public VscConverterStationImpl add() {
        String id = checkAndGetUniqueId();
        String name = getName();
        TerminalExt terminal = checkAndGetTerminal(id);
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, voltageSetPoint, reactivePowerSetPoint);
        VscConverterStationImpl converterStation
                = new VscConverterStationImpl(id, name, getNetwork().getRef(), voltageRegulatorOn, reactivePowerSetPoint, voltageSetPoint);
        converterStation.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getObjectStore().checkAndAdd(converterStation);
        getNetwork().getListeners().notifyCreation(converterStation);
        return converterStation;
    }

}
