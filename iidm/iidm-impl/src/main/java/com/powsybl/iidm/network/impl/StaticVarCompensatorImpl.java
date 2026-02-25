/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StaticVarCompensatorImpl extends AbstractConnectable<StaticVarCompensator> implements StaticVarCompensator {

    static final String TYPE_DESCRIPTION = "Static var compensator";

    private double bMin;

    private double bMax;

    private VoltageRegulationImpl voltageRegulation;

    // attributes depending on the variant
    private final TDoubleArrayList targetQ;
    private final TDoubleArrayList targetV;

    StaticVarCompensatorImpl(String id, String name, boolean fictitious, double bMin, double bMax,
                             VoltageRegulationImpl voltageRegulation, Ref<NetworkImpl> ref, double targetQ, double targetV) {
        super(ref, id, name, fictitious);
        this.bMin = bMin;
        this.bMax = bMax;
        this.voltageRegulation = voltageRegulation;
        if (this.voltageRegulation != null) {
            this.voltageRegulation.updateValidable(this);
        }
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.targetQ = new TDoubleArrayList(variantArraySize);
        this.targetV = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.targetQ.add(targetQ);
            this.targetV.add(targetV);
        }
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    protected String getTypeDescription() {
        return TYPE_DESCRIPTION;
    }

    @Override
    public double getBmin() {
        return bMin;
    }

    @Override
    public StaticVarCompensatorImpl setBmin(double bMin) {
        ValidationUtil.checkBmin(this, bMin);
        double oldValue = this.bMin;
        this.bMin = bMin;
        notifyUpdate("bMin", oldValue, bMin);
        return this;
    }

    @Override
    public double getBmax() {
        return bMax;
    }

    @Override
    public StaticVarCompensatorImpl setBmax(double bMax) {
        ValidationUtil.checkBmax(this, bMax);
        double oldValue = this.bMax;
        this.bMax = bMax;
        notifyUpdate("bMax", oldValue, bMax);
        return this;
    }

    @Override
    public double getTargetV() {
        return this.targetV.get(getNetwork().getVariantIndex());
    }

    @Override
    public double getTargetQ() {
        return this.targetQ.get(getNetwork().getVariantIndex());
    }

    @Override
    public double getVoltageSetpoint() {
        return this.getRegulatingTargetV();
    }

    @Override
    public StaticVarCompensatorImpl setVoltageSetpoint(double voltageSetpoint) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            if (isRegulatingWithMode(RegulationMode.VOLTAGE)) {
                double oldValue = regulation.getTargetValue();
                regulation.setTargetValue(voltageSetpoint);
                String variantId = getNetwork().getVariantManager().getVariantId(getNetwork().getVariantIndex());
                notifyUpdate("voltageSetpoint", variantId, oldValue, voltageSetpoint);
            }
        });
        return this;
    }

    @Override
    public StaticVarCompensator setTargetQ(double targetQ) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkDoublePositive(this, targetQ, "targetQ");
        int variantIndex = n.getVariantIndex();
        double oldValue = this.targetQ.set(variantIndex, targetQ);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetQ", variantId, oldValue, targetQ);
        return this;
    }

    @Override
    public StaticVarCompensator setTargetV(double targetV) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkDoublePositive(this, targetV, "targetV");
        int variantIndex = n.getVariantIndex();
        double oldValue = this.targetV.set(variantIndex, targetV);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetV", variantId, oldValue, targetV);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return this.getRegulatingTargetQ();
    }

    @Override
    public StaticVarCompensatorImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            if (isRegulatingWithMode(RegulationMode.REACTIVE_POWER)) {
                double oldValue = regulation.getTargetValue();
                regulation.setTargetValue(reactivePowerSetpoint);
                String variantId = getNetwork().getVariantManager().getVariantId(getNetwork().getVariantIndex());
                notifyUpdate("reactivePowerSetpoint", variantId, oldValue, reactivePowerSetpoint);
            }
        });
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return getOptionalVoltageRegulation().map(VoltageRegulation::getMode).orElse(null);
    }

    @Override
    public StaticVarCompensatorImpl setRegulationMode(RegulationMode regulationMode) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            RegulationMode oldValue = regulation.getMode();
            regulation.setMode(regulationMode);
            String variantId = getNetwork().getVariantManager().getVariantId(getNetwork().getVariantIndex());
            notifyUpdate("regulationMode", variantId, oldValue, regulationMode);
        });
        return this;
    }

    @Override
    public StaticVarCompensatorImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            Terminal oldValue = regulation.getTerminal();
            regulation.setTerminal(regulatingTerminal);
            String variantId = getNetwork().getVariantManager().getVariantId(getNetwork().getVariantIndex());
            notifyUpdate("regulatingTerminal", variantId, oldValue, regulatingTerminal);
        });
        return this;
    }

    @Override
    public void remove() {
        this.removeVoltageRegulation();
        super.remove();
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        for (int i = 0; i < number; i++) {
            targetQ.add(targetQ.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
        }
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.extendVariantArraySize(initVariantArraySize, number, sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        targetQ.remove(targetQ.size() - number, number);
        targetV.remove(targetV.size() - number, number);
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.deleteVariantArrayElement(number));
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.deleteVariantArrayElement(index));
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            targetQ.set(index, targetQ.get(sourceIndex));
            targetV.set(index, targetV.get(sourceIndex));
        }
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.allocateVariantArrayElement(indexes, sourceIndex));
    }

    @Override
    public boolean isRegulating() {
        return getOptionalVoltageRegulation().map(VoltageRegulation::isRegulating).orElse(false);
    }

    @Override
    public StaticVarCompensator setRegulating(boolean regulating) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            boolean oldValue = regulation.isRegulating();
            regulation.setRegulating(regulating);
            String variantId = getNetwork().getVariantManager().getVariantId(getNetwork().getVariantIndex());
            notifyUpdate("regulating", variantId, oldValue, regulating);
        });
        return this;
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl<>(StaticVarCompensator.class, this, getNetwork().getRef(), this::setVoltageRegulation);
    }

    @Override
    public VoltageRegulation newVoltageRegulation(VoltageRegulation voltageRegulation) {
        this.setVoltageRegulation((VoltageRegulationImpl) voltageRegulation);
        return this.voltageRegulation;
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        return this.voltageRegulation;
    }

    private Optional<VoltageRegulationImpl> getOptionalVoltageRegulation() {
        return Optional.ofNullable(this.voltageRegulation);
    }

    @Override
    public void removeVoltageRegulation() {
        this.getOptionalVoltageRegulation().ifPresent(VoltageRegulationImpl::removeTerminal);
        this.voltageRegulation = null;
    }

    private void setVoltageRegulation(VoltageRegulationImpl voltageRegulation) {
        this.removeVoltageRegulation();
        this.voltageRegulation = voltageRegulation;
    }

}
