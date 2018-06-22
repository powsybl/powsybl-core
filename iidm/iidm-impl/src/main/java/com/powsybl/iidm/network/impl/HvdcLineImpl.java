/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.BitSet;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class HvdcLineImpl extends AbstractIdentifiable<HvdcLine> implements HvdcLine, Stateful {

    static final String TYPE_DESCRIPTION = "hvdcLine";

    private double r;

    private double nominalV;

    private double maxP;

    // attributes depending on the state

    private final BitSet convertersMode;

    private final TDoubleArrayList activePowerSetpoint;

    //

    private final AbstractHvdcConverterStation<?> converterStation1;

    private final AbstractHvdcConverterStation<?> converterStation2;

    private final Ref<NetworkImpl> networkRef;

    HvdcLineImpl(String id, String name, double r, double nominalV, double maxP, ConvertersMode convertersMode, double activePowerSetpoint,
                 AbstractHvdcConverterStation<?> converterStation1, AbstractHvdcConverterStation<?> converterStation2,
                 Ref<NetworkImpl> networkRef) {
        super(id, name);
        this.r = r;
        this.nominalV = nominalV;
        this.maxP = maxP;
        int stateArraySize = networkRef.get().getStateManager().getStateArraySize();
        this.convertersMode = new BitSet(stateArraySize);
        this.convertersMode.set(0, stateArraySize, convertersMode == ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER);
        this.activePowerSetpoint = new TDoubleArrayList(stateArraySize);
        this.activePowerSetpoint.fill(0, stateArraySize, activePowerSetpoint);
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
    public double getR() {
        return r;
    }

    @Override
    public HvdcLineImpl setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = this.r;
        this.r = r;
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public double getNominalV() {
        return nominalV;
    }

    @Override
    public HvdcLineImpl setNominalV(double nominalV) {
        ValidationUtil.checkNominalV(this, nominalV);
        double oldValue = this.nominalV;
        this.nominalV = nominalV;
        notifyUpdate("nominalV", oldValue, nominalV);
        return this;
    }

    @Override
    public double getMaxP() {
        return maxP;
    }

    @Override
    public HvdcLineImpl setMaxP(double maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        double oldValue = this.maxP;
        this.maxP = maxP;
        notifyUpdate("maxP", oldValue, maxP);
        return this;
    }

    @Override
    public double getActivePowerSetpoint() {
        return activePowerSetpoint.get(getNetwork().getStateIndex());
    }

    @Override
    public HvdcLineImpl setActivePowerSetpoint(double activePowerSetpoint) {
        ValidationUtil.checkActivePowerSetpoint(this, activePowerSetpoint);
        double oldValue = this.activePowerSetpoint.set(getNetwork().getStateIndex(), activePowerSetpoint);
        notifyUpdate("activePowerSetpoint", oldValue, activePowerSetpoint);
        return this;
    }

    @Override
    public AbstractHvdcConverterStation<?> getConverterStation1() {
        return converterStation1;
    }

    @Override
    public AbstractHvdcConverterStation<?> getConverterStation2() {
        return converterStation2;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        convertersMode.set(initStateArraySize, initStateArraySize + number, convertersMode.get(sourceIndex));

        activePowerSetpoint.ensureCapacity(activePowerSetpoint.size() + number);
        activePowerSetpoint.fill(initStateArraySize, initStateArraySize + number, activePowerSetpoint.get(sourceIndex));
    }

    @Override
    public void reduceStateArraySize(int number) {
        activePowerSetpoint.remove(activePowerSetpoint.size() - number, number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            convertersMode.set(index, convertersMode.get(sourceIndex));
            activePowerSetpoint.set(index, activePowerSetpoint.get(sourceIndex));
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
        return TYPE_DESCRIPTION;
    }
}
