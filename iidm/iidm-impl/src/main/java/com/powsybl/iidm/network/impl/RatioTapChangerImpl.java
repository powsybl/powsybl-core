/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.*;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class RatioTapChangerImpl extends AbstractTapChanger<RatioTapChangerParent, RatioTapChangerImpl, RatioTapChangerStepImpl> implements RatioTapChanger {

    private VoltageRegulationExt voltageRegulation;

    RatioTapChangerImpl(RatioTapChangerParent parent, int lowTapPosition,
                        List<RatioTapChangerStepImpl> steps, boolean loadTapChangingCapabilities,
                        Integer tapPosition, Integer solvedTapPosition, VoltageRegulationExt voltageRegulation) {
        super(parent, lowTapPosition, steps, loadTapChangingCapabilities, tapPosition, solvedTapPosition, "ratio tap changer");
        this.voltageRegulation = voltageRegulation;
        this.attachVoltageRegulation();
    }

    protected void notifyUpdate(Supplier<String> attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(parent.getTransformer(), attribute, oldValue, newValue);
    }

    protected void notifyUpdate(Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(parent.getTransformer(), attribute, variantId, oldValue, newValue);
    }

    @Override
    protected RegulatingPoint createRegulatingPoint(int variantArraySize, boolean regulating) {
        return new RegulatingPoint(parent.getTransformer().getId(), () -> null, variantArraySize, regulating, true);
    }

    @Override
    protected Integer getRelativeNeutralPosition() {
        for (int i = 0; i < steps.size(); i++) {
            RatioTapChangerStepImpl step = steps.get(i);
            if (step.getRho() == 1) {
                return i;
            }
        }
        return null;
    }

    @Override
    public RatioTapChangerStepsReplacerImpl stepsReplacer() {
        return new RatioTapChangerStepsReplacerImpl(this);
    }

    @Override
    public Optional<RatioTapChangerStep> getNeutralStep() {
        return relativeNeutralPosition != null ? Optional.of(steps.get(relativeNeutralPosition)) : Optional.empty();
    }

    @Override
    public RatioTapChangerImpl setRegulating(boolean regulating) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkRatioTapChangerRegulation(parent, regulating, loadTapChangingCapabilities,
            getRegulatingTerminal(), getRegulationMode(), getRegulationValue(), n,
            n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating,
            n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        n.invalidateValidationLevel();
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            boolean oldValue = regulation.isRegulating();
            int variantIndex = n.getVariantIndex();
            regulation.setRegulating(regulating);
            String variantId = n.getVariantManager().getVariantId(variantIndex);

            n.invalidateValidationLevel();
            notifyUpdate(() -> getTapChangerAttribute() + ".regulating", variantId, oldValue, regulating);
        });
        return this;
    }

    @Override
    public Terminal getRegulationTerminal() {
        return this.getRegulatingTerminal();
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return loadTapChangingCapabilities;
    }

    @Override
    public RatioTapChangerImpl setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), loadTapChangingCapabilities,
            getRegulatingTerminal(), getRegulationMode(),
            getRegulationValue(), n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        boolean oldValue = this.loadTapChangingCapabilities;
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        n.invalidateValidationLevel();
        notifyUpdate(() -> getTapChangerAttribute() + ".loadTapChangingCapabilities", oldValue, loadTapChangingCapabilities);
        return this;
    }

    @Override
    public RatioTapChangerImpl setTargetV(double targetV) {
        double oldValue = Double.NaN;
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), loadTapChangingCapabilities, getRegulatingTerminal(),
            RegulationMode.VOLTAGE, targetV, n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        if (voltageRegulation != null) {
            if (!Double.isNaN(targetV) && !isWithMode(RegulationMode.VOLTAGE)) {
                RegulationMode oldMode = voltageRegulation.getMode();
                newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetValue(targetV)
                    .withTerminal(voltageRegulation.getTerminal())
                    .withTargetDeadband(voltageRegulation.getTargetDeadband())
                    .withSlope(voltageRegulation.getSlope())
                    .build();
                n.invalidateValidationLevel();
                notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", variantId, oldMode, RegulationMode.VOLTAGE);
            } else if (isWithMode(RegulationMode.VOLTAGE)) {
                oldValue = voltageRegulation.getTargetValue();
                voltageRegulation.setTargetValue(targetV);
                n.invalidateValidationLevel();
            }
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(targetV)
                .withRegulating(false)
                .build();
        }
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", variantId, oldValue, targetV);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return this.voltageRegulation != null ? this.voltageRegulation.getMode() : null;
    }

    @Override
    public RatioTapChangerImpl setRegulationMode(RegulationMode regulationMode) {
        RegulationMode oldValue = null;
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getMode();
            newVoltageRegulation()
                .withMode(regulationMode)
                .withTerminal(voltageRegulation.getTerminal())
                .withTargetValue(voltageRegulation.getTargetValue())
                .withTargetDeadband(voltageRegulation.getTargetDeadband())
                .withRegulating(voltageRegulation.isRegulating())
                .withSlope(voltageRegulation.getSlope())
                .build();
        } else {
            newVoltageRegulation()
                .withMode(regulationMode)
                .withRegulating(false)
                .build();
        }
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", oldValue, regulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return this.getOptionalVoltageRegulation().map(VoltageRegulation::getTargetValue).orElse(Double.NaN);
    }

    @Override
    public RatioTapChangerImpl setRegulationValue(double regulationValue) {
        double oldValue = Double.NaN;
        int variantIndex = network.get().getVariantIndex();
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getTargetValue();
            voltageRegulation.setTargetValue(regulationValue);
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE) // Default regulation mode
                .withRegulating(false)
                .withTargetValue(regulationValue)
                .build();
        }
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", variantId, oldValue, regulationValue);
        return this;
    }

    @Override
    public double getTargetV() {
        return getRegulatingTargetV();
    }

    @Override
    public RatioTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        Terminal oldValue = null;
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), loadTapChangingCapabilities, regulationTerminal,
            getRegulationMode(), getRegulationValue(), n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        n.invalidateValidationLevel();
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getTerminal();
            voltageRegulation.setTerminal(regulationTerminal, voltageRegulation.getTargetValue());
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE) // Default regulation mode
                .withRegulating(false)
                .withTerminal(regulationTerminal)
                .build();
        }
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationTerminal", variantId, oldValue, regulationTerminal);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return getOptionalVoltageRegulation().map(VoltageRegulationExt::getTargetDeadband).orElse(Double.NaN);
    }

    @Override
    public RatioTapChanger setTargetDeadband(double targetDeadband) {
        double oldValue = Double.NaN;
        int variantIndex = network.get().getVariantIndex();
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getTargetDeadband();
            voltageRegulation.setTargetDeadband(targetDeadband);
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE) // Default regulation mode
                .withRegulating(false)
                .withTargetDeadband(targetDeadband)
                .build();
        }
        notifyUpdate(() -> getTapChangerAttribute() + ".targetDeadband", variantId, oldValue, targetDeadband);
        return this;
    }

    @Override
    public void remove() {
        super.remove();
        parent.setRatioTapChanger(null);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        getOptionalVoltageRegulation().ifPresent(vr -> vr.extendVariantArraySize(initVariantArraySize, number, sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        getOptionalVoltageRegulation().ifPresent(vr -> vr.reduceVariantArraySize(number));
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        getOptionalVoltageRegulation().ifPresent(vr -> vr.deleteVariantArrayElement(index));
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        getOptionalVoltageRegulation().ifPresent(vr -> vr.allocateVariantArrayElement(indexes, sourceIndex));
    }

    @Override
    protected String getTapChangerAttribute() {
        return "ratio" + parent.getTapChangerAttribute();
    }

    @Override
    public Map<Integer, RatioTapChangerStep> getAllSteps() {
        Map<Integer, RatioTapChangerStep> allSteps = new HashMap<>();
        for (int i = 0; i < steps.size(); i++) {
            allSteps.put(i + lowTapPosition, steps.get(i));
        }
        return allSteps;
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(RatioTapChanger.class, parent, this, getNetwork().getRef(), this::setVoltageRegulation);
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        return this.voltageRegulation;
    }

    private Optional<VoltageRegulationExt> getOptionalVoltageRegulation() {
        return Optional.ofNullable(this.voltageRegulation);
    }

    @Override
    public void removeVoltageRegulation() {
        this.getOptionalVoltageRegulation().ifPresent(VoltageRegulationExt::removeTerminal);
        this.voltageRegulation = null;

    }

    @Override
    public Terminal getTerminal() {
        return null;
    }

    @Override
    public VoltageRegulationHolder setLocalTargetV(double targetV) {
        return null;
    }

    private void setVoltageRegulation(VoltageRegulationExt voltageRegulation) {
        getOptionalVoltageRegulation().ifPresent(VoltageRegulationExt::remove);
        this.voltageRegulation = voltageRegulation;
        this.attachVoltageRegulation();
    }

    private void attachVoltageRegulation() {
        getOptionalVoltageRegulation().ifPresent(vr -> {
            vr.updateValidable(parent);
            vr.setHolder(this);
        });
    }
}
