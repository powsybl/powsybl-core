/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.util.DanglingLineBoundaryImpl;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineImpl extends AbstractConnectable<DanglingLine> implements DanglingLine {

    static class GenerationImpl implements Generation, ReactiveLimitsOwner, Validable {

        private DanglingLineImpl danglingLine;

        private ReactiveLimitsHolderImpl reactiveLimits;

        private double minP;

        private double maxP;

        // attributes depending on the variant

        private final TDoubleArrayList targetP;

        private final TDoubleArrayList targetQ;

        private final TDoubleArrayList targetV;

        private final List<Boolean> voltageRegulationOn;

        GenerationImpl(VariantManagerHolder network, double minP, double maxP, double targetP, double targetQ, double targetV, Boolean voltageRegulationOn) {
            this.minP = Double.isNaN(minP) ? -Double.MAX_VALUE : minP;
            this.maxP = Double.isNaN(maxP) ? Double.MAX_VALUE : maxP;

            int variantArraySize = network.getVariantManager().getVariantArraySize();
            this.targetP = new TDoubleArrayList(variantArraySize);
            this.targetQ = new TDoubleArrayList(variantArraySize);
            this.targetV = new TDoubleArrayList(variantArraySize);
            this.voltageRegulationOn = new ArrayList<>(variantArraySize);
            for (int i = 0; i < variantArraySize; i++) {
                this.targetP.add(targetP);
                this.targetQ.add(targetQ);
                this.targetV.add(targetV);
                this.voltageRegulationOn.add(voltageRegulationOn);
            }
        }

        GenerationImpl attach(DanglingLineImpl danglingLine) {
            if (this.danglingLine != null) {
                throw new AssertionError("DanglingLine.Generation already attached to " + this.danglingLine.getId());
            }

            this.danglingLine = Objects.requireNonNull(danglingLine);
            this.reactiveLimits = new ReactiveLimitsHolderImpl(this.danglingLine, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));

            return this;
        }

        @Override
        public double getTargetP() {
            return targetP.get(danglingLine.getNetwork().getVariantIndex());
        }

        @Override
        public GenerationImpl setTargetP(double targetP) {
            NetworkImpl n = danglingLine.getNetwork();
            ValidationUtil.checkActivePowerSetpoint(danglingLine, targetP, n.getMinValidationLevel().compareTo(ValidationLevel.LOADFLOW) >= 0);
            int variantIndex = danglingLine.network.get().getVariantIndex();
            double oldValue = this.targetP.set(variantIndex, targetP);
            String variantId = danglingLine.network.get().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            danglingLine.notifyUpdate("targetP", variantId, oldValue, targetP);
            return this;
        }

        @Override
        public double getMaxP() {
            return maxP;
        }

        @Override
        public GenerationImpl setMaxP(double maxP) {
            ValidationUtil.checkMaxP(danglingLine, maxP);
            ValidationUtil.checkActivePowerLimits(danglingLine, minP, maxP);
            double oldValue = this.maxP;
            this.maxP = maxP;
            danglingLine.notifyUpdate("maxP", oldValue, maxP);
            return this;
        }

        @Override
        public double getMinP() {
            return minP;
        }

        @Override
        public GenerationImpl setMinP(double minP) {
            ValidationUtil.checkMinP(danglingLine, minP);
            ValidationUtil.checkActivePowerLimits(danglingLine, minP, maxP);
            double oldValue = this.minP;
            this.minP = minP;
            danglingLine.notifyUpdate("minP", oldValue, minP);
            return this;
        }

        @Override
        public double getTargetQ() {
            return targetQ.get(danglingLine.getNetwork().getVariantIndex());
        }

        @Override
        public GenerationImpl setTargetQ(double targetQ) {
            NetworkImpl n = danglingLine.getNetwork();
            int variantIndex = danglingLine.network.get().getVariantIndex();
            ValidationUtil.checkVoltageControl(danglingLine, voltageRegulationOn.get(variantIndex), targetV.get(variantIndex), targetQ, n.getMinValidationLevel());
            double oldValue = this.targetQ.set(variantIndex, targetQ);
            String variantId = danglingLine.network.get().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            danglingLine.notifyUpdate("targetQ", variantId, oldValue, targetQ);
            return this;
        }

        @Override
        public boolean isVoltageRegulationOn() {
            return Optional.ofNullable(voltageRegulationOn.get(danglingLine.getNetwork().getVariantIndex()))
                    .orElseThrow(ValidationUtil::createUndefinedValueGetterException);
        }

        @Override
        public Optional<Boolean> findVoltageRegulationStatus() {
            return Optional.ofNullable(voltageRegulationOn.get(danglingLine.getNetwork().getVariantIndex()));
        }

        @Override
        public GenerationImpl setVoltageRegulationOn(boolean voltageRegulationOn) {
            NetworkImpl n = danglingLine.getNetwork();
            int variantIndex = danglingLine.getNetwork().getVariantIndex();
            ValidationUtil.checkVoltageControl(danglingLine, voltageRegulationOn,
                    targetV.get(variantIndex), targetQ.get(variantIndex), n.getMinValidationLevel());
            Boolean oldValue = this.voltageRegulationOn.get(variantIndex);
            this.voltageRegulationOn.set(variantIndex, voltageRegulationOn);
            String variantId = danglingLine.getNetwork().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            danglingLine.notifyUpdate("voltageRegulationOn", variantId, oldValue, voltageRegulationOn);
            return this;
        }

        @Override
        public Generation unsetVoltageRegulationOn() {
            NetworkImpl n = danglingLine.getNetwork();
            int variantIndex = danglingLine.getNetwork().getVariantIndex();
            ValidationUtil.checkVoltageControl(danglingLine, null,
                    targetV.get(variantIndex), targetQ.get(variantIndex), n.getMinValidationLevel());
            Boolean oldValue = this.voltageRegulationOn.get(variantIndex);
            this.voltageRegulationOn.set(variantIndex, null);
            String variantId = danglingLine.getNetwork().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            danglingLine.notifyUpdate("voltageRegulationOn", variantId, oldValue, null);
            return this;
        }

        @Override
        public double getTargetV() {
            return this.targetV.get(danglingLine.getNetwork().getVariantIndex());
        }

        @Override
        public GenerationImpl setTargetV(double targetV) {
            NetworkImpl n = danglingLine.getNetwork();
            int variantIndex = danglingLine.getNetwork().getVariantIndex();
            ValidationUtil.checkVoltageControl(danglingLine, voltageRegulationOn.get(variantIndex),
                    targetV, targetQ.get(variantIndex), n.getMinValidationLevel());
            double oldValue = this.targetV.set(variantIndex, targetV);
            String variantId = danglingLine.getNetwork().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            danglingLine.notifyUpdate("targetV", variantId, oldValue, targetV);
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
        public String getMessageHeader() {
            return danglingLine.getMessageHeader();
        }

        void extendVariantArraySize(int number, int sourceIndex) {
            targetP.ensureCapacity(targetP.size() + number);
            targetQ.ensureCapacity(targetQ.size() + number);
            ((ArrayList<Boolean>) voltageRegulationOn).ensureCapacity(voltageRegulationOn.size() + number);
            targetV.ensureCapacity(targetV.size() + number);
            for (int i = 0; i < number; i++) {
                targetP.add(targetP.get(sourceIndex));
                targetQ.add(targetQ.get(sourceIndex));
                voltageRegulationOn.add(voltageRegulationOn.get(sourceIndex));
                targetV.add(targetV.get(sourceIndex));
            }
        }

        void reduceVariantArraySize(int number) {
            targetP.remove(targetP.size() - number, number);
            targetQ.remove(targetQ.size() - number, number);
            List<Boolean> tmp = new ArrayList<>(voltageRegulationOn.subList(0, number));
            voltageRegulationOn.clear();
            voltageRegulationOn.addAll(tmp);
            targetV.remove(targetV.size() - number, number);

        }

        void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
            for (int index : indexes) {
                targetP.set(index, targetP.get(sourceIndex));
                targetQ.set(index, targetQ.get(sourceIndex));
                voltageRegulationOn.set(index, voltageRegulationOn.get(sourceIndex));
                targetV.set(index, targetV.get(sourceIndex));
            }
        }
    }

    private final Ref<? extends VariantManagerHolder> network;

    private double r;

    private double x;

    private double g;

    private double b;

    private final String ucteXnodeCode;

    private final GenerationImpl generation;

    private final OperationalLimitsHolderImpl operationalLimitsHolder;
    // attributes depending on the variant

    private final TDoubleArrayList p0;

    private final TDoubleArrayList q0;

    private final DanglingLineBoundaryImpl boundary;

    DanglingLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious, double p0, double q0, double r, double x, double g, double b, String ucteXnodeCode, GenerationImpl generation) {
        super(network, id, name, fictitious);
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
        this.ucteXnodeCode = ucteXnodeCode;
        this.operationalLimitsHolder = new OperationalLimitsHolderImpl(this, "limits");
        this.boundary = new DanglingLineBoundaryImpl(this);
        this.generation = generation != null ? generation.attach(this) : null;
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    protected String getTypeDescription() {
        return "Dangling line";
    }

    @Override
    public double getP0() {
        return p0.get(network.get().getVariantIndex());
    }

    @Override
    public DanglingLineImpl setP0(double p0) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkP0(this, p0, n.getMinValidationLevel());
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.p0.set(variantIndex, p0);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("p0", variantId, oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return q0.get(network.get().getVariantIndex());
    }

    @Override
    public DanglingLineImpl setQ0(double q0) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkQ0(this, q0, n.getValidationLevel());
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.q0.set(variantIndex, q0);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("q0", variantId, oldValue, q0);
        return this;
    }

    @Override
    public double getR() {
        return r;
    }

    @Override
    public DanglingLineImpl setR(double r) {
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
    public DanglingLineImpl setX(double x) {
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
    public DanglingLineImpl setG(double g) {
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
    public DanglingLineImpl setB(double b) {
        ValidationUtil.checkB(this, b);
        double oldValue = this.b;
        this.b = b;
        notifyUpdate("b", oldValue, b);
        return this;
    }

    @Override
    public String getUcteXnodeCode() {
        return ucteXnodeCode;
    }

    @Override
    public Generation getGeneration() {
        return generation;
    }

    @Override
    public Collection<OperationalLimits> getOperationalLimits() {
        return operationalLimitsHolder.getOperationalLimits();
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
    }

    @Override
    public ActivePowerLimits getActivePowerLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
    }

    @Override
    public ApparentPowerLimits getApparentPowerLimits() {
        return operationalLimitsHolder.getOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return operationalLimitsHolder.newCurrentLimits();
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return operationalLimitsHolder.newActivePowerLimits();
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return operationalLimitsHolder.newApparentPowerLimits();
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
            generation.extendVariantArraySize(number, sourceIndex);
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
