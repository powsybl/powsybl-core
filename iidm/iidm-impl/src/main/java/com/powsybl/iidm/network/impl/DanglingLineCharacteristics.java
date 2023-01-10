/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Boundary;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Objects;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class DanglingLineCharacteristics {

    static class GenerationImpl implements DanglingLine.Generation, ReactiveLimitsOwner, Validable {

        private AbstractConnectable<?> danglingLine;

        private ReactiveLimitsHolderImpl reactiveLimits;

        private double minP;

        private double maxP;

        // attributes depending on the variant

        private final TDoubleArrayList targetP;

        private final TDoubleArrayList targetQ;

        private final TDoubleArrayList targetV;

        private final TBooleanArrayList voltageRegulationOn;

        GenerationImpl(VariantManagerHolder network, double minP, double maxP, double targetP, double targetQ, double targetV, boolean voltageRegulationOn) {
            this.minP = Double.isNaN(minP) ? -Double.MAX_VALUE : minP;
            this.maxP = Double.isNaN(maxP) ? Double.MAX_VALUE : maxP;

            int variantArraySize = network.getVariantManager().getVariantArraySize();
            this.targetP = new TDoubleArrayList(variantArraySize);
            this.targetQ = new TDoubleArrayList(variantArraySize);
            this.targetV = new TDoubleArrayList(variantArraySize);
            this.voltageRegulationOn = new TBooleanArrayList(variantArraySize);
            for (int i = 0; i < variantArraySize; i++) {
                this.targetP.add(targetP);
                this.targetQ.add(targetQ);
                this.targetV.add(targetV);
                this.voltageRegulationOn.add(voltageRegulationOn);
            }
        }

        GenerationImpl attach(AbstractConnectable<?> parent) {
            if (this.danglingLine != null) {
                throw new AssertionError("DanglingLine.Generation already attached to " + this.danglingLine.getId());
            }
            this.danglingLine = Objects.requireNonNull(parent);
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
            ValidationUtil.checkActivePowerSetpoint(danglingLine, targetP, n.getMinValidationLevel());
            int variantIndex = danglingLine.getNetwork().getVariantIndex();
            double oldValue = this.targetP.set(variantIndex, targetP);
            String variantId = danglingLine.getNetwork().getVariantManager().getVariantId(variantIndex);
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
            int variantIndex = n.getVariantIndex();
            ValidationUtil.checkVoltageControl(danglingLine, voltageRegulationOn.get(variantIndex), targetV.get(variantIndex), targetQ, n.getMinValidationLevel());
            double oldValue = this.targetQ.set(variantIndex, targetQ);
            String variantId = n.getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            danglingLine.notifyUpdate("targetQ", variantId, oldValue, targetQ);
            return this;
        }

        @Override
        public boolean isVoltageRegulationOn() {
            return voltageRegulationOn.get(danglingLine.getNetwork().getVariantIndex());
        }

        @Override
        public GenerationImpl setVoltageRegulationOn(boolean voltageRegulationOn) {
            NetworkImpl n = danglingLine.getNetwork();
            int variantIndex = danglingLine.getNetwork().getVariantIndex();
            ValidationUtil.checkVoltageControl(danglingLine, voltageRegulationOn,
                    targetV.get(variantIndex), targetQ.get(variantIndex), n.getMinValidationLevel());
            boolean oldValue = this.voltageRegulationOn.get(variantIndex);
            this.voltageRegulationOn.set(variantIndex, voltageRegulationOn);
            String variantId = danglingLine.getNetwork().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            danglingLine.notifyUpdate("voltageRegulationOn", variantId, oldValue, voltageRegulationOn);
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
            voltageRegulationOn.ensureCapacity(voltageRegulationOn.size() + number);
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
            voltageRegulationOn.remove(voltageRegulationOn.size() - number, number);
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

    private final AbstractConnectable<?> parent;

    private double r;

    private double x;

    private double g;

    private double b;

    private final String ucteXnodeCode;

    private final GenerationImpl generation;

    // attributes depending on the variant

    private final TDoubleArrayList p0;

    private final TDoubleArrayList q0;

    private final Boundary boundary;

    DanglingLineCharacteristics(AbstractConnectable<?> parent, Boundary boundary,
                                double p0, double q0, double r, double x, double g, double b, String ucteXnodeCode, GenerationImpl generation) {
        this.parent = parent;
        int variantArraySize = parent.getNetwork().getVariantManager().getVariantArraySize();
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
        this.boundary = boundary;
        this.generation = generation != null ? generation.attach(parent) : null;
    }

    double getP0() {
        return p0.get(parent.getNetwork().getVariantIndex());
    }

    void setP0(double p0, boolean check) {
        NetworkImpl n = parent.getNetwork();
        if (check) {
            ValidationUtil.checkP0(parent, p0, n.getMinValidationLevel());
        }
        int variantIndex = n.getVariantIndex();
        double oldValue = this.p0.set(variantIndex, p0);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        parent.notifyUpdate("p0", variantId, oldValue, p0);
    }

    double getQ0() {
        return q0.get(parent.getNetwork().getVariantIndex());
    }

    void setQ0(double q0, boolean check) {
        NetworkImpl n = parent.getNetwork();
        if (check) {
            ValidationUtil.checkQ0(parent, q0, n.getValidationLevel());
        }
        int variantIndex = n.getVariantIndex();
        double oldValue = this.q0.set(variantIndex, q0);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        parent.notifyUpdate("q0", variantId, oldValue, q0);
    }

    double getR() {
        return r;
    }

    void setR(double r) {
        ValidationUtil.checkR(parent, r);
        double oldValue = this.r;
        this.r = r;
        parent.notifyUpdate("r", oldValue, r);
    }

    double getX() {
        return x;
    }

    void setX(double x) {
        ValidationUtil.checkX(parent, x);
        double oldValue = this.x;
        this.x = x;
        parent.notifyUpdate("x", oldValue, x);
    }

    double getG() {
        return g;
    }

    void setG(double g) {
        ValidationUtil.checkG(parent, g);
        double oldValue = this.g;
        this.g = g;
        parent.notifyUpdate("g", oldValue, g);
    }

    double getB() {
        return b;
    }

    void setB(double b) {
        ValidationUtil.checkB(parent, b);
        double oldValue = this.b;
        this.b = b;
        parent.notifyUpdate("b", oldValue, b);
    }

    String getUcteXnodeCode() {
        return ucteXnodeCode;
    }

    DanglingLine.Generation getGeneration() {
        return generation;
    }

    Boundary getBoundary() {
        return boundary;
    }

    void extendVariantArraySize(int number, int sourceIndex) {
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

    void reduceVariantArraySize(int number) {
        p0.remove(p0.size() - number, number);
        q0.remove(q0.size() - number, number);
        if (generation != null) {
            generation.reduceVariantArraySize(number);
        }
    }

    void deleteVariantArrayElement(int index) {
        // nothing to do
    }

    void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            p0.set(index, p0.get(sourceIndex));
            q0.set(index, q0.get(sourceIndex));
        }
        if (generation != null) {
            generation.allocateVariantArrayElement(indexes, sourceIndex);
        }
    }
}
