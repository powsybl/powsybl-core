/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.LccConverterStationAdder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class LccConverterStationAdderImpl extends AbstractHvdcConverterStationAdder<LccConverterStationAdderImpl> implements LccConverterStationAdder {

    private float powerFactor = Float.NaN;

    LccConverterStationAdderImpl(VoltageLevelExt voltageLevel) {
        super(voltageLevel);
    }

    @Override
    protected String getTypeDescription() {
        return LccConverterStationImpl.TYPE_DESCRIPTION;
    }

    @Override
    public LccConverterStationAdder setPowerFactor(float powerFactor) {
        this.powerFactor = powerFactor;
        return this;
    }

    @Override
    public LccConverterStation add() {
        String id = checkAndGetUniqueId();
        String name = getName();
        TerminalExt terminal = checkAndGetTerminal();
        validate();
        LccConverterStationImpl converterStation
                = new LccConverterStationImpl(id, name, getLossFactor(), powerFactor);
        converterStation.addTerminal(terminal);
        getVoltageLevel().attach(terminal, false);
        getNetwork().getObjectStore().checkAndAdd(converterStation);
        getNetwork().getListeners().notifyCreation(converterStation);
        return converterStation;
    }

    @Override
    protected void validate() {
        super.validate();

        ValidationUtil.checkPowerFactor(this, powerFactor);
    }

}
