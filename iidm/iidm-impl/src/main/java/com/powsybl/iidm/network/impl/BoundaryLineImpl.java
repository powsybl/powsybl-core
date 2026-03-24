/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.util.fastutil.ExtendedBooleanArrayList;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class BoundaryLineImpl extends AbstractConnectable<BoundaryLine> implements BoundaryLine {

    static class GenerationImpl implements Generation, ReactiveLimitsOwner, Validable {

        private BoundaryLineImpl boundaryLine;

        private ReactiveLimitsHolderImpl reactiveLimits;

        private double minP;

        private double maxP;

        // attributes depending on the variant

        private final ExtendedDoubleArrayList targetP;

        private final ExtendedDoubleArrayList targetQ;

        private final ExtendedDoubleArrayList targetV;

        private final ExtendedBooleanArrayList voltageRegulationOn;

        GenerationImpl(VariantManagerHolder network, double minP, double maxP, double targetP, double targetQ, double targetV, boolean voltageRegulationOn) {
            this.minP = Double.isNaN(minP) ? -Double.MAX_VALUE : minP;
            this.maxP = Double.isNaN(maxP) ? Double.MAX_VALUE : maxP;

            int variantArraySize = network.getVariantManager().getVariantArraySize();
            this.targetP = new ExtendedDoubleArrayList(variantArraySize, targetP);
            this.targetQ = new ExtendedDoubleArrayList(variantArraySize, targetQ);
            this.targetV = new ExtendedDoubleArrayList(variantArraySize, targetV);
            this.voltageRegulationOn = new ExtendedBooleanArrayList(variantArraySize, voltageRegulationOn);
        }

        GenerationImpl attach(BoundaryLineImpl boundaryLine) {
            if (this.boundaryLine != null) {
                throw new IllegalStateException("BoundaryLine.Generation already attached to " + this.boundaryLine.getId());
            }

            this.boundaryLine = Objects.requireNonNull(boundaryLine);
            this.reactiveLimits = new ReactiveLimitsHolderImpl(this.boundaryLine, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));

            return this;
        }

        @Override
        public double getTargetP() {
            return targetP.getDouble(boundaryLine.getNetwork().getVariantIndex());
        }

        @Override
        public GenerationImpl setTargetP(double targetP) {
            NetworkImpl n = boundaryLine.getNetwork();
            ValidationUtil.checkActivePowerSetpoint(boundaryLine, targetP, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
            int variantIndex = boundaryLine.network.get().getVariantIndex();
            double oldValue = this.targetP.set(variantIndex, targetP);
            String variantId = boundaryLine.network.get().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            boundaryLine.notifyUpdate("targetP", variantId, oldValue, targetP);
            return this;
        }

        @Override
        public double getMaxP() {
            return maxP;
        }

        @Override
        public GenerationImpl setMaxP(double maxP) {
            ValidationUtil.checkMaxP(boundaryLine, maxP);
            ValidationUtil.checkActivePowerLimits(boundaryLine, minP, maxP);
            double oldValue = this.maxP;
            this.maxP = maxP;
            boundaryLine.notifyUpdate("maxP", oldValue, maxP);
            return this;
        }

        @Override
        public double getMinP() {
            return minP;
        }

        @Override
        public GenerationImpl setMinP(double minP) {
            ValidationUtil.checkMinP(boundaryLine, minP);
            ValidationUtil.checkActivePowerLimits(boundaryLine, minP, maxP);
            double oldValue = this.minP;
            this.minP = minP;
            boundaryLine.notifyUpdate("minP", oldValue, minP);
            return this;
        }

        @Override
        public double getTargetQ() {
            return targetQ.getDouble(boundaryLine.getNetwork().getVariantIndex());
        }

        @Override
        public GenerationImpl setTargetQ(double targetQ) {
            NetworkImpl n = boundaryLine.getNetwork();
            int variantIndex = n.getVariantIndex();
            ValidationUtil.checkVoltageControl(boundaryLine, voltageRegulationOn.getBoolean(variantIndex), targetV.getDouble(variantIndex), targetQ,
                    n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
            double oldValue = this.targetQ.set(variantIndex, targetQ);
            String variantId = n.getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            boundaryLine.notifyUpdate("targetQ", variantId, oldValue, targetQ);
            return this;
        }

        @Override
        public boolean isVoltageRegulationOn() {
            return voltageRegulationOn.getBoolean(boundaryLine.getNetwork().getVariantIndex());
        }

        @Override
        public GenerationImpl setVoltageRegulationOn(boolean voltageRegulationOn) {
            NetworkImpl n = boundaryLine.getNetwork();
            int variantIndex = boundaryLine.getNetwork().getVariantIndex();
            ValidationUtil.checkVoltageControl(boundaryLine, voltageRegulationOn, targetV.getDouble(variantIndex), targetQ.getDouble(variantIndex),
                    n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
            boolean oldValue = this.voltageRegulationOn.getBoolean(variantIndex);
            this.voltageRegulationOn.set(variantIndex, voltageRegulationOn);
            String variantId = boundaryLine.getNetwork().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            boundaryLine.notifyUpdate("voltageRegulationOn", variantId, oldValue, voltageRegulationOn);
            return this;
        }

        @Override
        public NetworkImpl getNetwork() {
            return this.boundaryLine.getNetwork();
        }

        @Override
        public double getTargetV() {
            return this.targetV.getDouble(boundaryLine.getNetwork().getVariantIndex());
        }

        @Override
        public GenerationImpl setTargetV(double targetV) {
            NetworkImpl n = boundaryLine.getNetwork();
            int variantIndex = boundaryLine.getNetwork().getVariantIndex();
            ValidationUtil.checkVoltageControl(boundaryLine, voltageRegulationOn.getBoolean(variantIndex), targetV, targetQ.getDouble(variantIndex),
                    n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
            double oldValue = this.targetV.set(variantIndex, targetV);
            String variantId = boundaryLine.getNetwork().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            boundaryLine.notifyUpdate("targetV", variantId, oldValue, targetV);
            return this;
        }

        @Override
        public ReactiveCapabilityCurveAdderImpl newReactiveCapabilityCurve() {
            return new ReactiveCapabilityCurveAdderImpl<>(this);
        }

        @Override
        public MinMaxReactiveLimitsAdderImpl newMinMaxReactiveLimits() {
            return new MinMaxReactiveLimitsAdderImpl<>(this);
        }

        @Override
        public void setReactiveLimits(ReactiveLimits reactiveLimits) {
            this.reactiveLimits.setReactiveLimits(reactiveLimits);
        }

        @Override
        public ReactiveLimits getReactiveLimits() {
            return reactiveLimits.getReactiveLimits();
        }

        @Override
        public <R extends ReactiveLimits> R getReactiveLimits(Class<R> type) {
            return reactiveLimits.getReactiveLimits(type);
        }

        @Override
        public MessageHeader getMessageHeader() {
            return boundaryLine.getMessageHeader();
        }

        void extendVariantArraySize(int number, int sourceIndex) {
            targetP.growAndFill(number, targetP.getDouble(sourceIndex));
            targetQ.growAndFill(number, targetQ.getDouble(sourceIndex));
            targetV.growAndFill(number, targetV.getDouble(sourceIndex));
            voltageRegulationOn.growAndFill(number, voltageRegulationOn.getBoolean(sourceIndex));
        }

        void reduceVariantArraySize(int number) {
            targetP.removeElements(number);
            targetQ.removeElements(number);
            voltageRegulationOn.removeElements(number);
            targetV.removeElements(number);
        }

        void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
            for (int index : indexes) {
                targetP.set(index, targetP.getDouble(sourceIndex));
                targetQ.set(index, targetQ.getDouble(sourceIndex));
                voltageRegulationOn.set(index, voltageRegulationOn.getBoolean(sourceIndex));
                targetV.set(index, targetV.getDouble(sourceIndex));
            }
        }
    }

    private final Ref<NetworkImpl> network;
    private TieLineImpl tieLine = null;

    private double r;

    private double x;

    private double g;

    private double b;

    private String pairingKey;

    private final GenerationImpl generation;

    private final OperationalLimitsGroupsImpl operationalLimitsGroups;
    // attributes depending on the variant

    private final ExtendedDoubleArrayList p0;

    private final ExtendedDoubleArrayList q0;

    private final BoundaryLineBoundaryImplExt boundary;

    BoundaryLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious, double p0, double q0, double r, double x, double g, double b, String pairingKey, GenerationImpl generation) {
        super(network, id, name, fictitious);
        this.network = network;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.p0 = new ExtendedDoubleArrayList(variantArraySize, p0);
        this.q0 = new ExtendedDoubleArrayList(variantArraySize, q0);
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
        this.pairingKey = pairingKey;
        this.operationalLimitsGroups = new OperationalLimitsGroupsImpl(this, "limits");
        this.boundary = new BoundaryLineBoundaryImplExt(this);
        this.generation = generation != null ? generation.attach(this) : null;
    }

    @Override
    void replaceId(String newId) {
        NetworkIndex.checkId(newId);
        network.get().getIndex().remove(this);
        id = newId;
        network.get().getIndex().checkAndAdd(this);
    }

    void setTieLine(TieLineImpl tieLine) {
        this.tieLine = tieLine;
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public Optional<TieLine> getTieLine() {
        return Optional.ofNullable(tieLine);
    }

    @Override
    public void remove() {
        if (tieLine != null) {
            throw new UnsupportedOperationException("Parent tie line " + tieLine.getId() + " should be removed before the child boundary line");
        }
        super.remove();
        boundary.remove();
    }

    void removeTieLine() {
        tieLine = null;
    }

    @Override
    protected String getTypeDescription() {
        return "Boundary line";
    }

    @Override
    public boolean isPaired() {
        return tieLine != null;
    }

    @Override
    public double getP0() {
        return p0.getDouble(network.get().getVariantIndex());
    }

    @Override
    public BoundaryLineImpl setP0(double p0) {
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        double oldValue = this.p0.set(variantIndex, p0);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("p0", variantId, oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return q0.getDouble(network.get().getVariantIndex());
    }

    @Override
    public BoundaryLineImpl setQ0(double q0) {
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        double oldValue = this.q0.set(variantIndex, q0);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("q0", variantId, oldValue, q0);
        return this;
    }

    @Override
    public double getR() {
        return r;
    }

    @Override
    public BoundaryLineImpl setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = this.r;
        this.r = r;
        notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public BoundaryLineImpl setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = this.x;
        this.x = x;
        notifyUpdate("x", oldValue, x);
        return this;
    }

    @Override
    public double getG() {
        return g;
    }

    @Override
    public BoundaryLineImpl setG(double g) {
        ValidationUtil.checkG(this, g);
        double oldValue = this.g;
        this.g = g;
        notifyUpdate("g", oldValue, g);
        return this;
    }

    @Override
    public double getB() {
        return b;
    }

    @Override
    public BoundaryLineImpl setB(double b) {
        ValidationUtil.checkB(this, b);
        double oldValue = this.b;
        this.b = b;
        notifyUpdate("b", oldValue, b);
        return this;
    }

    @Override
    public String getPairingKey() {
        return pairingKey;
    }

    @Override
    public BoundaryLine setPairingKey(String pairingKey) {
        if (this.isPaired()) {
            throw new ValidationException(this, "pairing key cannot be set if boundary line is paired.");
        } else {
            String oldValue = this.pairingKey;
            this.pairingKey = pairingKey;
            notifyUpdate("pairing_key", oldValue, pairingKey);
        }
        return this;
    }

    @Override
    public Generation getGeneration() {
        return generation;
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups() {
        return operationalLimitsGroups.getOperationalLimitsGroups();
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId() {
        return operationalLimitsGroups.getSelectedOperationalLimitsGroupId();
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id) {
        return operationalLimitsGroups.getOperationalLimitsGroup(id);
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup() {
        return operationalLimitsGroups.getSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup newOperationalLimitsGroup(String id) {
        return operationalLimitsGroups.newOperationalLimitsGroup(id);
    }

    @Override
    public void setSelectedOperationalLimitsGroup(String id) {
        operationalLimitsGroups.setSelectedOperationalLimitsGroup(id);
    }

    @Override
    public void addSelectedOperationalLimitsGroups(String... ids) {
        operationalLimitsGroups.addSelectedOperationalLimitsGroups(ids);
    }

    @Override
    public void removeOperationalLimitsGroup(String id) {
        operationalLimitsGroups.removeOperationalLimitsGroup(id);
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup() {
        operationalLimitsGroups.cancelSelectedOperationalLimitsGroup();
    }

    @Override
    public OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup() {
        return operationalLimitsGroups.getOrCreateSelectedOperationalLimitsGroup();
    }

    @Override
    public Collection<String> getAllSelectedOperationalLimitsGroupIds() {
        return operationalLimitsGroups.getAllSelectedOperationalLimitsGroupIds();
    }

    @Override
    public List<String> getAllSelectedOperationalLimitsGroupIdsOrdered() {
        return operationalLimitsGroups.getAllSelectedOperationalLimitsGroupIdsOrdered();
    }

    @Override
    public Collection<OperationalLimitsGroup> getAllSelectedOperationalLimitsGroups() {
        return operationalLimitsGroups.getAllSelectedOperationalLimitsGroups();
    }

    @Override
    public void deselectOperationalLimitsGroups(String... ids) {
        operationalLimitsGroups.deselectOperationalLimitsGroups(ids);
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return operationalLimitsGroups.getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return operationalLimitsGroups.getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits();
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return operationalLimitsGroups.getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits();
    }

    @Override
    public Boundary getBoundary() {
        return boundary;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        p0.growAndFill(number, p0.getDouble(sourceIndex));
        q0.growAndFill(number, q0.getDouble(sourceIndex));
        if (generation != null) {
            generation.extendVariantArraySize(number, sourceIndex);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        p0.removeElements(number);
        q0.removeElements(number);
        if (generation != null) {
            generation.reduceVariantArraySize(number);
        }
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
            p0.set(index, p0.getDouble(sourceIndex));
            q0.set(index, q0.getDouble(sourceIndex));
        }
        if (generation != null) {
            generation.allocateVariantArrayElement(indexes, sourceIndex);
        }
    }

}
