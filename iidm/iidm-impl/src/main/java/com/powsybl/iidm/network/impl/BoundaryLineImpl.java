/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.*;
import gnu.trove.list.array.TDoubleArrayList;

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

        private final TDoubleArrayList targetP;

        private final TDoubleArrayList localTargetV;

        private final TDoubleArrayList localTargetQ;

        private VoltageRegulationExt voltageRegulation;

        private final VoltageRegulation.VoltageRegulationAttributes initialVoltageRegulation;

        GenerationImpl(VariantManagerHolder network, double minP, double maxP, double targetP,
                       double localTargetV, double localTargetQ, VoltageRegulation.VoltageRegulationAttributes initialVoltageRegulation) {
            this.minP = Double.isNaN(minP) ? -Double.MAX_VALUE : minP;
            this.maxP = Double.isNaN(maxP) ? Double.MAX_VALUE : maxP;

            int variantArraySize = network.getVariantManager().getVariantArraySize();
            this.targetP = new TDoubleArrayList(variantArraySize);
            this.localTargetV = new TDoubleArrayList(variantArraySize);
            this.localTargetQ = new TDoubleArrayList(variantArraySize);
            for (int i = 0; i < variantArraySize; i++) {
                this.targetP.add(targetP);
                this.localTargetV.add(localTargetV);
                this.localTargetQ.add(localTargetQ);
            }
            this.initialVoltageRegulation = initialVoltageRegulation;
        }

        GenerationImpl attach(BoundaryLineImpl boundaryLine) {
            if (this.boundaryLine != null) {
                throw new IllegalStateException("BoundaryLine.Generation already attached to " + this.boundaryLine.getId());
            }

            this.boundaryLine = Objects.requireNonNull(boundaryLine);
            this.reactiveLimits = new ReactiveLimitsHolderImpl(this.boundaryLine, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));
            if (initialVoltageRegulation != null) {
                createOrUpdateVoltageRegulation(initialVoltageRegulation);
            }

            return this;
        }

        @Override
        public double getTargetP() {
            return targetP.get(boundaryLine.getNetwork().getVariantIndex());
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
            return getLocalTargetQ();
        }

        @Override
        public GenerationImpl setTargetQ(double targetQ) {
            return setLocalTargetQ(targetQ);
        }

        @Override
        public boolean isVoltageRegulationOn() {
            return voltageRegulation != null && voltageRegulation.isRegulating();
        }

        @Override
        public GenerationImpl setVoltageRegulationOn(boolean voltageRegulationOn) {
            if (voltageRegulation != null) {
                voltageRegulation.setRegulating(voltageRegulationOn);
            } else if (voltageRegulationOn) {
                createOrUpdateVoltageRegulation(new VoltageRegulation.VoltageRegulationAttributes(
                        Double.NaN, Double.NaN, Double.NaN, RegulationMode.VOLTAGE, true));
            }
            return this;
        }

        @Override
        public NetworkImpl getNetwork() {
            return this.boundaryLine.getNetwork();
        }

        @Override
        public double getTargetV() {
            return getLocalTargetV();
        }

        @Override
        public GenerationImpl setTargetV(double targetV) {
            return setLocalTargetV(targetV);
        }

        @Override
        public Terminal getTerminal() {
            return boundaryLine.getTerminal();
        }

        @Override
        public VoltageRegulationExt getVoltageRegulation() {
            return voltageRegulation;
        }

        @Override
        public VoltageRegulationBuilder newVoltageRegulation() {
            return new VoltageRegulationBuilderImpl(BoundaryLine.Generation.class, boundaryLine, this, boundaryLine.getNetwork().getRef(), this::createOrUpdateVoltageRegulation);
        }

        @Override
        public void removeVoltageRegulation() {
            NetworkImpl n = boundaryLine.getNetwork();
            ValidationUtil.checkLocalTargetQandV(boundaryLine,
                    BoundaryLine.Generation.class,
                    getLocalTargetV(),
                    getLocalTargetQ(),
                    true,
                    false,
                    false,
                    null,
                    n.getMinValidationLevel(),
                    n.getReportNodeContext().getReportNode());
            if (voltageRegulation != null) {
                voltageRegulation.onRemove();
                voltageRegulation = null;
            }
        }

        @Override
        public double getLocalTargetV() {
            return localTargetV.get(boundaryLine.getNetwork().getVariantIndex());
        }

        @Override
        public GenerationImpl setLocalTargetV(double localTargetV) {
            NetworkImpl n = boundaryLine.getNetwork();
            ValidationUtil.checkLocalTargetQandV(boundaryLine,
                    BoundaryLine.Generation.class,
                    localTargetV,
                    getLocalTargetQ(),
                    voltageRegulation,
                    n.getMinValidationLevel(),
                    n.getReportNodeContext().getReportNode());
            int variantIndex = n.getVariantIndex();
            double oldValue = this.localTargetV.set(variantIndex, localTargetV);
            String variantId = n.getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            boundaryLine.notifyUpdate("localTargetV", variantId, oldValue, localTargetV);
            return this;
        }

        @Override
        public double getLocalTargetQ() {
            return localTargetQ.get(boundaryLine.getNetwork().getVariantIndex());
        }

        @Override
        public GenerationImpl setLocalTargetQ(double localTargetQ) {
            NetworkImpl n = boundaryLine.getNetwork();
            ValidationUtil.checkLocalTargetQandV(boundaryLine,
                    BoundaryLine.Generation.class,
                    getLocalTargetV(),
                    localTargetQ,
                    voltageRegulation,
                    n.getMinValidationLevel(),
                    n.getReportNodeContext().getReportNode());
            int variantIndex = n.getVariantIndex();
            double oldValue = this.localTargetQ.set(variantIndex, localTargetQ);
            String variantId = n.getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            boundaryLine.notifyUpdate("localTargetQ", variantId, oldValue, localTargetQ);
            return this;
        }

        private VoltageRegulationExt createOrUpdateVoltageRegulation(VoltageRegulation.VoltageRegulationAttributes attributes) {
            if (voltageRegulation == null) {
                voltageRegulation = VoltageRegulationImpl.createVoltageRegulation(boundaryLine, this, BoundaryLine.Generation.class, boundaryLine.getNetwork().getRef(), attributes);
            } else {
                voltageRegulation.setAttributesOnCurrentVariant(attributes);
            }
            return voltageRegulation;
        }

        @Override
        public ReactiveCapabilityCurveAdderImpl newReactiveCapabilityCurve() {
            return new ReactiveCapabilityCurveAdderImpl<>(this);
        }

        @Override
        public ReactiveCapabilityShapeAdderImpl newReactiveCapabilityShape() {
            return new ReactiveCapabilityShapeAdderImpl(this);
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

        void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
            localTargetV.ensureCapacity(localTargetV.size() + number);
            localTargetQ.ensureCapacity(localTargetQ.size() + number);
            for (int i = 0; i < number; i++) {
                localTargetV.add(localTargetV.get(sourceIndex));
                localTargetQ.add(localTargetQ.get(sourceIndex));
            }
            if (voltageRegulation != null) {
                voltageRegulation.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
            }
            targetP.ensureCapacity(targetP.size() + number);
            for (int i = 0; i < number; i++) {
                targetP.add(targetP.get(sourceIndex));
            }
        }

        void reduceVariantArraySize(int number) {
            localTargetV.remove(localTargetV.size() - number, number);
            localTargetQ.remove(localTargetQ.size() - number, number);
            if (voltageRegulation != null) {
                voltageRegulation.reduceVariantArraySize(number);
            }
            targetP.remove(targetP.size() - number, number);
        }

        void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
            for (int index : indexes) {
                localTargetV.set(index, localTargetV.get(sourceIndex));
                localTargetQ.set(index, localTargetQ.get(sourceIndex));
            }
            if (voltageRegulation != null) {
                voltageRegulation.allocateVariantArrayElement(indexes, sourceIndex);
            }
            for (int index : indexes) {
                targetP.set(index, targetP.get(sourceIndex));
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

    private final TDoubleArrayList p0;

    private final TDoubleArrayList q0;

    private final BoundaryLineBoundaryImplExt boundary;

    BoundaryLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious, boolean equivalent,
                     double p0, double q0, double r, double x, double g, double b, String pairingKey, GenerationImpl generation) {
        super(network, id, name, fictitious, equivalent);
        this.network = network;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.p0 = new TDoubleArrayList(variantArraySize);
        this.q0 = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.p0.add(p0);
            this.q0.add(q0);
        }
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
        this.pairingKey = pairingKey;
        this.operationalLimitsGroups = new OperationalLimitsGroupsImpl(this, "limits");
        this.boundary = new BoundaryLineBoundaryImplExt(this);
        this.generation = generation != null ? generation.attach(this) : null;
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
        return p0.get(network.get().getVariantIndex());
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
        return q0.get(network.get().getVariantIndex());
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
    public List<OperationalLimitsGroup> getAllSelectedOperationalLimitsGroups() {
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
        p0.ensureCapacity(p0.size() + number);
        q0.ensureCapacity(q0.size() + number);
        for (int i = 0; i < number; i++) {
            p0.add(p0.get(sourceIndex));
            q0.add(q0.get(sourceIndex));
        }
        if (generation != null) {
            generation.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        p0.remove(p0.size() - number, number);
        q0.remove(q0.size() - number, number);
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
            p0.set(index, p0.get(sourceIndex));
            q0.set(index, q0.get(sourceIndex));
        }
        if (generation != null) {
            generation.allocateVariantArrayElement(indexes, sourceIndex);
        }
    }

}
