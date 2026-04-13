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

import java.util.*;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class RatioTapChangerImpl extends AbstractTapChanger<RatioTapChangerParent, RatioTapChangerImpl, RatioTapChangerStepImpl> implements RatioTapChanger {

    private VoltageRegulationImpl voltageRegulation;

    RatioTapChangerImpl(RatioTapChangerParent parent, int lowTapPosition,
                        List<RatioTapChangerStepImpl> steps, boolean loadTapChangingCapabilities,
                        Integer tapPosition, Integer solvedTapPosition, VoltageRegulationImpl voltageRegulation) {
        super(parent, lowTapPosition, steps, loadTapChangingCapabilities, tapPosition, solvedTapPosition, "ratio tap changer");
        this.voltageRegulation = voltageRegulation;
        if (this.voltageRegulation != null) {
            this.voltageRegulation.updateValidable(parent);
        }
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
    public boolean isRegulating() {
        return getOptionalVoltageRegulation().map(VoltageRegulationImpl::isRegulating).orElse(false);
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
        NetworkImpl n = getNetwork();
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), loadTapChangingCapabilities, getRegulatingTerminal(),
                RegulationMode.VOLTAGE, targetV, n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());

        getOptionalVoltageRegulation().ifPresent(regulation -> {
            if (!Double.isNaN(targetV)) {
                regulation.setMode(RegulationMode.VOLTAGE);
            }
            if (isWithMode(RegulationMode.VOLTAGE)) {
                double oldValue = regulation.getTargetValue();
                int variantIndex = n.getVariantIndex();
                regulation.setTargetValue(targetV);
                String variantId = n.getVariantManager().getVariantId(variantIndex);

                n.invalidateValidationLevel();
                notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", variantId, oldValue, targetV);
            }
        });
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return this.voltageRegulation != null ? this.voltageRegulation.getMode() : null;
    }

    @Override
    public RatioTapChangerImpl setRegulationMode(RegulationMode regulationMode) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            RegulationMode oldValue = regulation.getMode();
            regulation.setMode(regulationMode);
            notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", oldValue, regulationMode);
        });
        return this;
    }

    @Override
    public double getRegulationValue() {
        return this.getOptionalVoltageRegulation().map(VoltageRegulation::getTargetValue).orElse(Double.NaN);
    }

    @Override
    public RatioTapChangerImpl setRegulationValue(double regulationValue) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            double oldValue = regulation.getTargetValue();
            regulation.setTargetValue(regulationValue);
            int variantIndex = network.get().getVariantIndex();
            String variantId = network.get().getVariantManager().getVariantId(variantIndex);
            notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", variantId, oldValue, regulationValue);
        });
        return this;
    }

    @Override
    @Deprecated(forRemoval = true, since = "7.2.0")
    public RatioTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkRatioTapChangerRegulation(parent, isRegulating(), loadTapChangingCapabilities, regulationTerminal,
                getRegulationMode(), getRegulationValue(), n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        n.invalidateValidationLevel();
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            Terminal oldValue = regulation.getTerminal();
            regulation.setTerminal(regulationTerminal);
            int variantIndex = network.get().getVariantIndex();
            String variantId = network.get().getVariantManager().getVariantId(variantIndex);
            notifyUpdate(() -> getTapChangerAttribute() + ".regulationTerminal", variantId, oldValue, regulationTerminal);
        });
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return getOptionalVoltageRegulation().map(VoltageRegulationImpl::getTargetDeadband).orElse(Double.NaN);
    }

    @Override
    @Deprecated(forRemoval = true, since = "7.2.0")
    public RatioTapChanger setTargetDeadband(double targetDeadband) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            double oldValue = regulation.getTargetDeadband();
            regulation.setTargetDeadband(targetDeadband);
            int variantIndex = network.get().getVariantIndex();
            String variantId = network.get().getVariantManager().getVariantId(variantIndex);
            notifyUpdate(() -> getTapChangerAttribute() + ".targetDeadband", variantId, oldValue, targetDeadband);
        });
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
        return new VoltageRegulationBuilderImpl<>(RatioTapChanger.class, parent, getNetwork().getRef(), this::setVoltageRegulation);
    }

    @Override
    public VoltageRegulation newVoltageRegulation(VoltageRegulation voltageRegulation) {
        this.setVoltageRegulation((VoltageRegulationImpl) voltageRegulation);
        return null;
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

    @Override
    public Terminal getTerminal() {
        return null;
    }

    private void setVoltageRegulation(VoltageRegulationImpl voltageRegulation) {
        this.removeVoltageRegulation();
        this.voltageRegulation = voltageRegulation;
    }
}
