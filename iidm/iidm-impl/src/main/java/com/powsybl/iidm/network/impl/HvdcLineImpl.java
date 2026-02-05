/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SwitchPredicates;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class HvdcLineImpl extends AbstractIdentifiable<HvdcLine> implements HvdcLine {

    static final String TYPE_DESCRIPTION = "hvdcLine";

    private double r;

    private double nominalV;

    private double maxP;

    // attributes depending on the variant

    private final TIntArrayList convertersMode;

    private final TDoubleArrayList activePowerSetpoint;

    //

    private final Ref<NetworkImpl> networkRef;

    private AbstractHvdcConverterStation<?> converterStation1;

    private AbstractHvdcConverterStation<?> converterStation2;

    private boolean removed = false;

    HvdcLineImpl(String id, String name, boolean fictitious, double r, double nominalV, double maxP, ConvertersMode convertersMode, double activePowerSetpoint,
                 AbstractHvdcConverterStation<?> converterStation1, AbstractHvdcConverterStation<?> converterStation2,
                 Ref<NetworkImpl> networkRef) {
        super(id, name, fictitious);
        this.r = r;
        this.nominalV = nominalV;
        this.maxP = maxP;
        int variantArraySize = networkRef.get().getVariantManager().getVariantArraySize();
        this.convertersMode = new TIntArrayList(variantArraySize);
        this.convertersMode.fill(0, variantArraySize, convertersMode != null ? convertersMode.ordinal() : -1);
        this.activePowerSetpoint = new TDoubleArrayList(variantArraySize);
        this.activePowerSetpoint.fill(0, variantArraySize, activePowerSetpoint);
        this.converterStation1 = attach(converterStation1);
        this.converterStation2 = attach(converterStation2);
        this.networkRef = networkRef;
    }

    private AbstractHvdcConverterStation<?> attach(AbstractHvdcConverterStation<?> converterStation) {
        converterStation.setHvdcLine(this);
        return converterStation;
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    protected void notifyUpdate(String attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, variantId, oldValue, newValue);
    }

    @Override
    public NetworkImpl getNetwork() {
        if (removed) {
            throw new PowsyblException("Cannot access network of removed hvdc line " + id);
        }
        return networkRef.get();
    }

    @Override
    public Network getParentNetwork() {
        if (removed) {
            throw new PowsyblException("Cannot access parent network of removed hvdc line " + id);
        }
        // the parent network is the network that contains both terminals of converter stations.
        Network subnetwork1 = converterStation1.getParentNetwork();
        Network subnetwork2 = converterStation2.getParentNetwork();
        if (subnetwork1 == subnetwork2) {
            return subnetwork1;
        }
        return networkRef.get();
    }

    @Override
    public ConvertersMode getConvertersMode() {
        int variantIndex = networkRef.get().getVariantIndex();
        return convertersMode.get(variantIndex) != -1 ? ConvertersMode.values()[convertersMode.get(variantIndex)] : null;
    }

    @Override
    public HvdcLineImpl setConvertersMode(ConvertersMode convertersMode) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkConvertersMode(this, convertersMode, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        int variantIndex = n.getVariantIndex();
        ConvertersMode oldValue = this.convertersMode.get(variantIndex) != -1 ? ConvertersMode.values()[this.convertersMode.get(variantIndex)] : null;
        this.convertersMode.set(variantIndex, convertersMode != null ? convertersMode.ordinal() : -1);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("convertersMode", variantId, oldValue, convertersMode);
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
        ValidationUtil.checkHvdcMaxP(this, maxP);
        double oldValue = this.maxP;
        this.maxP = maxP;
        notifyUpdate("maxP", oldValue, maxP);
        return this;
    }

    @Override
    public double getActivePowerSetpoint() {
        return activePowerSetpoint.get(getNetwork().getVariantIndex());
    }

    @Override
    public HvdcLineImpl setActivePowerSetpoint(double activePowerSetpoint) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkHvdcActivePowerSetpoint(this, activePowerSetpoint,
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        int variantIndex = n.getVariantIndex();
        double oldValue = this.activePowerSetpoint.set(variantIndex, activePowerSetpoint);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("activePowerSetpoint", variantId, oldValue, activePowerSetpoint);
        return this;
    }

    @Override
    public AbstractHvdcConverterStation<?> getConverterStation(TwoSides side) {
        return (side == TwoSides.ONE) ? getConverterStation1() : getConverterStation2();
    }

    @Override
    public AbstractHvdcConverterStation<?> getConverterStation1() {
        if (removed) {
            throw new PowsyblException("Cannot access converter station of removed hvdc line " + id);
        }
        return converterStation1;
    }

    @Override
    public AbstractHvdcConverterStation<?> getConverterStation2() {
        if (removed) {
            throw new PowsyblException("Cannot access converter station of removed hvdc line " + id);
        }
        return converterStation2;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);

        convertersMode.ensureCapacity(convertersMode.size() + number);
        convertersMode.fill(initVariantArraySize, initVariantArraySize + number, convertersMode.get(sourceIndex));

        activePowerSetpoint.ensureCapacity(activePowerSetpoint.size() + number);
        activePowerSetpoint.fill(initVariantArraySize, initVariantArraySize + number, activePowerSetpoint.get(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);

        activePowerSetpoint.remove(activePowerSetpoint.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);

        for (int index : indexes) {
            convertersMode.set(index, convertersMode.get(sourceIndex));
            activePowerSetpoint.set(index, activePowerSetpoint.get(sourceIndex));
        }
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();

        network.getListeners().notifyBeforeRemoval(this);

        // Detach converter stations
        converterStation1.setHvdcLine(null);
        converterStation2.setHvdcLine(null);
        converterStation1 = null;
        converterStation2 = null;

        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
        removed = true;
    }

    @Override
    public boolean connectConverterStations() {
        return connectConverterStations(SwitchPredicates.IS_NONFICTIONAL_BREAKER, null);
    }

    @Override
    public boolean connectConverterStations(Predicate<Switch> isTypeSwitchToOperate) {
        return connectConverterStations(isTypeSwitchToOperate, null);
    }

    @Override
    public boolean connectConverterStations(Predicate<Switch> isTypeSwitchToOperate, TwoSides side) {
        return ConnectDisconnectUtil.connectAllTerminals(
            this,
            getTerminalsOfConverterStations(side),
            isTypeSwitchToOperate,
            getNetwork().getReportNodeContext().getReportNode());
    }

    @Override
    public boolean disconnectConverterStations() {
        return disconnectConverterStations(SwitchPredicates.IS_CLOSED_BREAKER, null);
    }

    @Override
    public boolean disconnectConverterStations(Predicate<Switch> isSwitchOpenable) {
        return disconnectConverterStations(isSwitchOpenable, null);
    }

    @Override
    public boolean disconnectConverterStations(Predicate<Switch> isSwitchOpenable, TwoSides side) {
        return ConnectDisconnectUtil.disconnectAllTerminals(
            this,
            getTerminalsOfConverterStations(side),
            isSwitchOpenable,
            getNetwork().getReportNodeContext().getReportNode());
    }

    private List<Terminal> getTerminalsOfConverterStations(TwoSides side) {
        return side == null ? List.of(getConverterStation1().getTerminal(), getConverterStation2().getTerminal()) : switch (side) {
            case ONE -> List.of(getConverterStation1().getTerminal());
            case TWO -> List.of(getConverterStation2().getTerminal());
        };
    }

    @Override
    protected String getTypeDescription() {
        return TYPE_DESCRIPTION;
    }
}
