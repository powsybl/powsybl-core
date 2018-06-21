/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class HvdcLineAdderImpl extends AbstractIdentifiableAdder<HvdcLineAdderImpl> implements HvdcLineAdder {

    private final Ref<NetworkImpl> networkRef;

    private double r = Double.NaN;

    private HvdcLine.ConvertersMode convertersMode;

    private double nominalV = Double.NaN;

    private double activePowerSetpoint = Double.NaN;

    private double maxP = Double.NaN;

    private String converterStationId1;

    private String converterStationId2;

    public HvdcLineAdderImpl(Ref<NetworkImpl> networkRef) {
        this.networkRef = Objects.requireNonNull(networkRef);
    }

    @Override
    protected NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return HvdcLineImpl.TYPE_DESCRIPTION;
    }

    @Override
    public HvdcLineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public HvdcLineAdder setConvertersMode(HvdcLine.ConvertersMode convertersMode) {
        this.convertersMode = convertersMode;
        return this;
    }

    @Override
    public HvdcLineAdder setNominalV(double nominalV) {
        this.nominalV = nominalV;
        return this;
    }

    @Override
    public HvdcLineAdder setActivePowerSetpoint(double activePowerSetpoint) {
        this.activePowerSetpoint = activePowerSetpoint;
        return this;
    }

    @Override
    public HvdcLineAdder setMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    @Override
    public HvdcLineAdder setConverterStationId1(String converterStationId1) {
        this.converterStationId1 = converterStationId1;
        return this;
    }

    @Override
    public HvdcLineAdder setConverterStationId2(String converterStationId2) {
        this.converterStationId2 = converterStationId2;
        return this;
    }

    @Override
    public HvdcLine add() {
        String id = checkAndGetUniqueId();
        String name = getName();
        ValidationUtil.checkR(this, r);
        ValidationUtil.checkConvertersMode(this, convertersMode);
        ValidationUtil.checkNominalV(this, nominalV);
        ValidationUtil.checkActivePowerSetpoint(this, activePowerSetpoint);
        ValidationUtil.checkMaxP(this, maxP);
        AbstractHvdcConverterStation<?> converterStation1 = getNetwork().getHvdcConverterStation(converterStationId1);
        if (converterStation1 == null) {
            throw new PowsyblException("Side 1 converter station " + converterStationId1 + " not found");
        }
        AbstractHvdcConverterStation<?> converterStation2 = getNetwork().getHvdcConverterStation(converterStationId2);
        if (converterStation2 == null) {
            throw new PowsyblException("Side 2 converter station " + converterStationId2 + " not found");
        }
        HvdcLineImpl hvdcLine = new HvdcLineImpl(id, name, r, nominalV, maxP, convertersMode, activePowerSetpoint,
                                                 converterStation1, converterStation2, networkRef);
        getNetwork().getObjectStore().checkAndAdd(hvdcLine);
        getNetwork().getListeners().notifyCreation(hvdcLine);
        return hvdcLine;
    }

}
