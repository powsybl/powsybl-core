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
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class RatioTapChangerImpl extends AbstractTapChanger<RatioTapChangerParent, RatioTapChangerImpl, RatioTapChangerStepImpl> implements RatioTapChanger {

    private VoltageRegulationExt voltageRegulation;

    RatioTapChangerImpl(RatioTapChangerParent parent, int lowTapPosition,
                        List<RatioTapChangerStepImpl> steps, boolean loadTapChangingCapabilities,
                        Integer tapPosition, Integer solvedTapPosition, VoltageRegulation.AttributesWithTerminal voltageRegulationAttributes) {
        super(parent, lowTapPosition, steps, loadTapChangingCapabilities, tapPosition, solvedTapPosition, "ratio tap changer");
        this.voltageRegulation = VoltageRegulationImpl.createVoltageRegulation(parent, this, RatioTapChanger.class, getNetwork().getRef(), voltageRegulationAttributes);
    }

    protected void notifyUpdate(Supplier<String> attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(parent.getTransformer(), attribute, oldValue, newValue);
    }

    protected void notifyUpdate(Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(parent.getTransformer(), attribute, variantId, oldValue, newValue);
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
        VoltageRegulation.AttributesWithTerminal attributes = getAttributes(a -> a.withRegulating(regulating));
        ValidationUtil.checkRatioTapChangerRegulation(parent, attributes,
                loadTapChangingCapabilities,
                n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating,
            n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        if (voltageRegulation != null) {
            boolean oldValue = voltageRegulation.isRegulating();
            int variantIndex = n.getVariantIndex();
            voltageRegulation.setRegulating(regulating);
            String variantId = n.getVariantManager().getVariantId(variantIndex);

            notifyUpdate(() -> getTapChangerAttribute() + ".regulating", variantId, oldValue, regulating);
        } else if (regulating) {
            // This will throw an exception because the target value is not set
            // (called to have the same message as everywhere else)
            newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withRegulating(true)
                    .build();
        }
        n.invalidateValidationLevel();
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
        VoltageRegulation.AttributesWithTerminal attributes = voltageRegulation != null ? voltageRegulation.getAttributes() : null;
        ValidationUtil.checkRatioTapChangerRegulation(parent, attributes,
                loadTapChangingCapabilities,
                n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
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
        VoltageRegulation.AttributesWithTerminal attributes = getAttributes(a -> a.withTargetValue(targetV).withMode(RegulationMode.VOLTAGE));
        ValidationUtil.checkRatioTapChangerRegulation(parent, attributes,
                loadTapChangingCapabilities,
                n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        if (voltageRegulation != null) {
            if (!Double.isNaN(targetV) && !isWithMode(RegulationMode.VOLTAGE)) {
                RegulationMode oldMode = voltageRegulation.getMode();
                voltageRegulation.setMode(RegulationMode.VOLTAGE);
                notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", variantId, oldMode, RegulationMode.VOLTAGE);
            }
            if (isWithMode(RegulationMode.VOLTAGE) && isRemoteRegulating()) {
                oldValue = voltageRegulation.getTargetValue();
                voltageRegulation.setTargetValue(targetV);
            }
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(targetV)
                .withRegulating(false)
                .build();
        }
        n.invalidateValidationLevel();
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", variantId, oldValue, targetV);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return this.voltageRegulation != null ? this.voltageRegulation.getMode() : null;
    }

    @Override
    public RatioTapChangerImpl setRegulationMode(RegulationMode newRegulationMode) {
        RegulationMode oldValue = null;
        if (voltageRegulation != null) {
            oldValue = voltageRegulation.getMode();
            voltageRegulation.setMode(newRegulationMode);
        } else {
            newVoltageRegulation()
                .withMode(newRegulationMode)
                .withRegulating(false)
                .build();
        }
        getNetwork().invalidateValidationLevel();
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", oldValue, newRegulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return voltageRegulation != null ? voltageRegulation.getTargetValue() : Double.NaN;
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
        getNetwork().invalidateValidationLevel();
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
        VoltageRegulation.AttributesWithTerminal attributes = getAttributes(a -> a.withTerminalAndTargetValue(regulationTerminal, getRegulationValue()));
        ValidationUtil.checkRatioTapChangerRegulation(parent, attributes,
                loadTapChangingCapabilities,
                n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
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
        n.invalidateValidationLevel();
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationTerminal", variantId, oldValue, regulationTerminal);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return voltageRegulation != null ? voltageRegulation.getTargetDeadband() : Double.NaN;
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
        if (voltageRegulation != null) {
            voltageRegulation.onRemove();
        }
        parent.setRatioTapChanger(null);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        if (voltageRegulation != null) {
            voltageRegulation.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        if (voltageRegulation != null) {
            voltageRegulation.reduceVariantArraySize(number);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        if (voltageRegulation != null) {
            voltageRegulation.deleteVariantArrayElement(index);
        }
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        if (voltageRegulation != null) {
            voltageRegulation.allocateVariantArrayElement(indexes, sourceIndex);
        }
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
        return new VoltageRegulationBuilderImpl(RatioTapChanger.class, parent, this, getNetwork().getRef(), this::createOrUpdateVoltageRegulation);
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        return this.voltageRegulation;
    }

    @Override
    public void removeVoltageRegulation() {
        if (voltageRegulation != null) {
            voltageRegulation.onRemove();
            voltageRegulation = null;
        }
    }

    private VoltageRegulation.AttributesWithTerminal getAttributes(UnaryOperator<VoltageRegulation.AttributesWithTerminal> modifier) {
        return voltageRegulation != null ? modifier.apply(voltageRegulation.getAttributes()) : null;
    }

    @Override
    public Terminal getTerminal() {
        return null;
    }

    @Override
    public RatioTapChangerImpl setLocalTargetV(double targetV) {
        return this;
    }

    /**
     * <p>
     * Creates or updates the voltage regulation corresponding to the provided attributes.
     * </p>
     * @param attributes The attributes to use for the VoltageRegulation object. Must not be null.
     * @return The updated or newly created voltageRegulation.
     */
    private VoltageRegulationExt createOrUpdateVoltageRegulation(VoltageRegulation.@NonNull AttributesWithTerminal attributes) {
        if (this.voltageRegulation == null) {
            this.voltageRegulation = VoltageRegulationImpl.createVoltageRegulation(parent, this, RatioTapChanger.class, getNetwork().getRef(), attributes);
        } else {
            this.voltageRegulation.setAttributesOnCurrentVariant(attributes);
        }
        return this.voltageRegulation;
    }
}
