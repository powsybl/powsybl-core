/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.HvdcLine;
import eu.itesla_project.iidm.network.HvdcLineAdder;
import eu.itesla_project.iidm.network.impl.util.Ref;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class HvdcLineAdderImpl extends IdentifiableAdderImpl<HvdcLineAdderImpl> implements HvdcLineAdder {

    private final Ref<NetworkImpl> networkRef;

    private float r = Float.NaN;

    private HvdcLine.ConvertersMode convertersMode;

    private float nominalV = Float.NaN;

    private float activePowerSetPoint = Float.NaN;

    private float maxP = Float.NaN;

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
    public HvdcLineAdder setR(float r) {
        this.r = r;
        return this;
    }

    @Override
    public HvdcLineAdder setConvertersMode(HvdcLine.ConvertersMode convertersMode) {
        this.convertersMode = convertersMode;
        return this;
    }

    @Override
    public HvdcLineAdder setNominalV(float nominalV) {
        this.nominalV = nominalV;
        return this;
    }

    @Override
    public HvdcLineAdder setActivePowerSetPoint(float targetP) {
        this.activePowerSetPoint = targetP;
        return this;
    }

    @Override
    public HvdcLineAdder setMaxP(float maxP) {
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
        ValidationUtil.checkActivePowerSetPoint(this, activePowerSetPoint);
        ValidationUtil.checkMaxP(this, maxP);
        HvdcConverterStationImpl<?> converterStation1 = getNetwork().getHvdcConverterStation(converterStationId1);
        if (converterStation1 == null) {
            throw new RuntimeException("Side 1 converter station " + converterStationId1 + " not found");
        }
        HvdcConverterStationImpl<?> converterStation2 = getNetwork().getHvdcConverterStation(converterStationId2);
        if (converterStation2 == null) {
            throw new RuntimeException("Side 2 converter station " + converterStationId2 + " not found");
        }
        HvdcLineImpl hvdcLine = new HvdcLineImpl(id, name, r, nominalV, maxP, convertersMode, activePowerSetPoint,
                                                 converterStation1, converterStation2, networkRef);
        getNetwork().getObjectStore().checkAndAdd(hvdcLine);
        getNetwork().getListeners().notifyCreation(hvdcLine);
        return hvdcLine;
    }

}
