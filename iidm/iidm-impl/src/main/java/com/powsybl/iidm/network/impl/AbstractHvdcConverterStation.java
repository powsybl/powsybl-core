/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.ValidationException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
abstract class AbstractHvdcConverterStation<T extends HvdcConverterStation<T>> extends AbstractConnectable<T> implements HvdcConverterStation<T> {

    private HvdcLine hvdcLine;

    private float lossFactor = Float.NaN;

    AbstractHvdcConverterStation(String id, String name, boolean fictitious, float lossFactor) {
        super(id, name, fictitious);
        this.hvdcLine = null;
        this.lossFactor = lossFactor;
    }

    @Override
    public HvdcLine getHvdcLine() {
        return hvdcLine;
    }

    T setHvdcLine(HvdcLine hvdcLine) {
        this.hvdcLine = hvdcLine;
        return (T) this;
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.HVDC_CONVERTER_STATION;
    }

    @Override
    public float getLossFactor() {
        return lossFactor;
    }

    @Override
    public T setLossFactor(float lossFactor) {
        ValidationUtil.checkLossFactor(this, lossFactor);
        float oldValue = this.lossFactor;
        this.lossFactor = lossFactor;
        notifyUpdate("lossFactor", oldValue, lossFactor);
        return (T) this;
    }

    @Override
    public void remove() {
        if (hvdcLine != null) {
            throw new ValidationException(this, "Impossible to remove this converter station (still attached to '" + hvdcLine.getId() + "')");
        }
        super.remove();
    }

}
