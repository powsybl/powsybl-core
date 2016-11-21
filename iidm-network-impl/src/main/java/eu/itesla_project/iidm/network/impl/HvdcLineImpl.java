/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.HvdcConverterStation;
import eu.itesla_project.iidm.network.HvdcLine;
import eu.itesla_project.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TFloatArrayList;

import java.util.BitSet;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class HvdcLineImpl extends IdentifiableImpl<HvdcLine> implements HvdcLine, Stateful {

    static final String TYPE_DESCRIPTION = "hvdcLine";

    private float r;

    private float nominalV;

    private float maxP;

    // attributes depending on the state

    private final BitSet convertersMode;

    private final TFloatArrayList activePowerSetPoint;

    //

    private final HvdcConverterStationImpl<?> converterStation1;

    private final HvdcConverterStationImpl<?> converterStation2;

    private final Ref<NetworkImpl> networkRef;

    HvdcLineImpl(String id, String name, float r, float nominalV, float maxP, ConvertersMode convertersMode, float activePowerSetPoint,
                 HvdcConverterStationImpl<?> converterStation1, HvdcConverterStationImpl<?> converterStation2,
                 Ref<NetworkImpl> networkRef) {
        super(id, name);
        this.r = r;
        this.nominalV = nominalV;
        this.maxP = maxP;
        int stateArraySize = networkRef.get().getStateManager().getStateArraySize();
        this.convertersMode = new BitSet(stateArraySize);
        this.convertersMode.set(0, stateArraySize, convertersMode == ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER);
        this.activePowerSetPoint = new TFloatArrayList(stateArraySize);
        this.activePowerSetPoint.fill(0, stateArraySize, activePowerSetPoint);
        this.converterStation1 = converterStation1;
        this.converterStation2 = converterStation2;
        this.networkRef = networkRef;
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    private static ConvertersMode toEnum(boolean convertersMode) {
        return convertersMode ? ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER : ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
    }

    private static boolean fromEnum(ConvertersMode convertersMode) {
        return convertersMode == ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
    }

    @Override
    public ConvertersMode getConvertersMode() {
        return toEnum(convertersMode.get(getNetwork().getStateIndex()));
    }

    @Override
    public HvdcLineImpl setConvertersMode(ConvertersMode convertersMode) {
        ValidationUtil.checkConvertersMode(this, convertersMode);
        int stateIndex = getNetwork().getStateIndex();
        boolean oldValue = this.convertersMode.get(stateIndex);
        this.convertersMode.set(stateIndex, fromEnum(Objects.requireNonNull(convertersMode)));
        notifyUpdate("convertersMode", toEnum(oldValue), convertersMode);
        return this;
    }

    @Override
    public float getR() {
        return r;
    }

    @Override
    public HvdcLineImpl setR(float r) {
        ValidationUtil.checkR(this, r);
        float oldValue = this.r;
        this.r = r;
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public float getNominalV() {
        return nominalV;
    }

    @Override
    public HvdcLineImpl setNominalV(float nominalV) {
        ValidationUtil.checkNominalV(this, nominalV);
        float oldValue = this.nominalV;
        this.nominalV = nominalV;
        notifyUpdate("nominalV", oldValue, nominalV);
        return this;
    }

    @Override
    public float getMaxP() {
        return maxP;
    }

    @Override
    public HvdcLineImpl setMaxP(float maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        float oldValue = this.maxP;
        this.maxP = maxP;
        notifyUpdate("maxP", oldValue, maxP);
        return this;
    }

    @Override
    public float getActivePowerSetPoint() {
        return activePowerSetPoint.get(getNetwork().getStateIndex());
    }

    @Override
    public HvdcLineImpl setActivePowerSetPoint(float activePowerSetPoint) {
        ValidationUtil.checkActivePowerSetPoint(this, activePowerSetPoint);
        float oldValue = this.activePowerSetPoint.set(getNetwork().getStateIndex(), activePowerSetPoint);
        notifyUpdate("targetP", oldValue, activePowerSetPoint);
        return this;
    }

    @Override
    public HvdcConverterStation<?> getConverterStation1() {
        return converterStation1;
    }

    @Override
    public HvdcConverterStation<?> getConverterStation2() {
        return converterStation2;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        convertersMode.set(initStateArraySize, initStateArraySize + number, convertersMode.get(sourceIndex));

        activePowerSetPoint.ensureCapacity(activePowerSetPoint.size() + number);
        activePowerSetPoint.fill(initStateArraySize, initStateArraySize + number, activePowerSetPoint.get(sourceIndex));
    }

    @Override
    public void reduceStateArraySize(int number) {
        activePowerSetPoint.remove(activePowerSetPoint.size() - number, number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            convertersMode.set(index, convertersMode.get(sourceIndex));
            activePowerSetPoint.set(index, activePowerSetPoint.get(sourceIndex));
        }
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();
        network.getObjectStore().remove(this);
        network.getListeners().notifyRemoval(this);
    }

    @Override
    protected String getTypeDescription() {
        return "hvdcLine";
    }
}
