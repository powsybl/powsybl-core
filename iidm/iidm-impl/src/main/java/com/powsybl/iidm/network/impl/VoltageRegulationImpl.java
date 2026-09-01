/**
 * Copyright (c) 2025-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationImpl implements VoltageRegulationExt {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageRegulationImpl.class);

    private static final String VOLTAGE_REGULATION_PREFIX = "VoltageRegulation.";

    protected enum NotifyUpdateKey {
        REGULATION_MODE(VOLTAGE_REGULATION_PREFIX + "RegulationMode"),
        REGULATING(VOLTAGE_REGULATION_PREFIX + "isRegulating"),
        TERMINAL(VOLTAGE_REGULATION_PREFIX + "Terminal"),
        SLOPE(VOLTAGE_REGULATION_PREFIX + "Slope"),
        TARGET_VALUE(VOLTAGE_REGULATION_PREFIX + "TargetValue"),
        TARGET_DEADBAND(VOLTAGE_REGULATION_PREFIX + "TargetDeadband");

        private final String key;

        NotifyUpdateKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }

    // Context
    private final Validable validable;
    private final VoltageRegulationHolder<?> holder;
    private final Class<? extends VoltageRegulationHolder<?>> classHolder;
    private final Ref<NetworkImpl> network;
    // Attributes
    private TerminalExt terminal;
    // Attributes depending on the variant
    private final TDoubleArrayList targetValue;
    private final TDoubleArrayList targetDeadband;
    private final TDoubleArrayList slope;
    private final TBooleanArrayList regulating;
    private final TIntArrayList regulationMode;
    //
    private static final int UNDEFINED_REGULATION_MODE = -1;

    protected static VoltageRegulationExt createVoltageRegulation(Validable validable,
                                                                  VoltageRegulationHolder<?> holder,
                                                                  Class<? extends VoltageRegulationHolder<?>> classHolder,
                                                                  Ref<NetworkImpl> network,
                                                                  VoltageRegulation.AttributesWithTerminal attributes) {
        return attributes != null ? new VoltageRegulationImpl(validable, holder, classHolder, network, attributes) : null;
    }

    protected VoltageRegulationImpl(Validable validable,
                                    VoltageRegulationHolder<?> holder,
                                    Class<? extends VoltageRegulationHolder<?>> classHolder,
                                    Ref<NetworkImpl> network,
                                    VoltageRegulation.AttributesWithTerminal attributes) {
        this.validable = validable;
        this.holder = holder;
        this.classHolder = classHolder;
        this.network = network;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.targetValue = new TDoubleArrayList(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        this.slope = new TDoubleArrayList(variantArraySize);
        this.regulating = new TBooleanArrayList(variantArraySize);
        this.regulationMode = new TIntArrayList(variantArraySize);
        initVariantAttributes(attributes.targetValue(),
            attributes.targetDeadband(),
            attributes.slope(),
            attributes.isRegulating(),
            attributes.mode(),
            variantArraySize);
        if (attributes.terminal() != null) {
            this.setTerminal(attributes.terminal(), attributes.targetValue());
        }
    }

    private void initVariantAttributes(double targetValue, double targetDeadband, double slope, boolean regulating, RegulationMode mode, int variantArraySize) {
        Integer regulationModeIndex = RegulationMode.getIndexFromMode(mode);
        for (int i = 0; i < variantArraySize; i++) {
            // When the VoltageRegulation object is created and there are already other variants,
            // it is created with "empty" values and defined as not regulating for the other variants.
            this.targetValue.add(Double.NaN);
            this.targetDeadband.add(Double.NaN);
            this.slope.add(Double.NaN);
            this.regulating.add(false);
            this.regulationMode.add(UNDEFINED_REGULATION_MODE);
        }
        int currentVariantIndex = getCurrentVariantIndex();
        this.targetValue.set(currentVariantIndex, targetValue);
        this.targetDeadband.set(currentVariantIndex, targetDeadband);
        this.slope.set(currentVariantIndex, slope);
        this.regulating.set(currentVariantIndex, regulating);
        this.regulationMode.set(currentVariantIndex, regulationModeIndex != null ? regulationModeIndex : UNDEFINED_REGULATION_MODE);
    }

    private void checkAttributes(AttributesWithTerminal newAttributes) {
        NetworkImpl n = network.get();
        ValidationUtil.checkVoltageRegulation(validable, newAttributes, n, classHolder,
            n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());

        if (n.getVariantManager().getVariantCount() > 1 && terminal != newAttributes.terminal()) {
            throw new PowsyblException(this.validable.getMessageHeader() + "Cannot change terminal when there are multiple variants");
        }
    }

    @Override
    public double getTargetValue() {
        return targetValue.get(getCurrentVariantIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true, then we validate the new value before setting it
     */
    @Override
    public VoltageRegulation setTargetValue(double newTargetValue) {
        AttributesWithTerminal newAttributes = this.getAttributes().withTargetValue(newTargetValue);
        checkAttributes(newAttributes);
        setTargetValueOnCurrentVariant(newTargetValue);
        return this;
    }

    private void setTargetValueOnCurrentVariant(double newTargetValue) {
        int currentVariantIndex = getCurrentVariantIndex();
        double oldTargetValue = this.targetValue.set(currentVariantIndex, newTargetValue);
        network.get().invalidateValidationLevel();
        String variantId = network.get().getVariantManager().getVariantId(currentVariantIndex);
        notifyUpdate(NotifyUpdateKey.TARGET_VALUE, variantId, oldTargetValue, newTargetValue);
    }

    @Override
    public double getTargetDeadband() {
        return targetDeadband.get(getCurrentVariantIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true, then we validate the new value before setting it
     */
    @Override
    public VoltageRegulation setTargetDeadband(double newTargetDeadband) {
        AttributesWithTerminal newAttributes = this.getAttributes().withTargetDeadband(newTargetDeadband);
        checkAttributes(newAttributes);
        setTargetDeadbandOnCurrentVariant(newTargetDeadband);
        return this;
    }

    private void setTargetDeadbandOnCurrentVariant(double newTargetDeadband) {
        int currentVariantIndex = getCurrentVariantIndex();
        double oldTargetDeadband = this.targetDeadband.set(currentVariantIndex, newTargetDeadband);
        network.get().invalidateValidationLevel();
        String variantId = network.get().getVariantManager().getVariantId(currentVariantIndex);
        notifyUpdate(NotifyUpdateKey.TARGET_DEADBAND, variantId, oldTargetDeadband, newTargetDeadband);
    }

    @Override
    public double getSlope() {
        return slope.get(getCurrentVariantIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true, then we validate the new value before setting it
     */
    @Override
    public VoltageRegulation setSlope(double newSlope) {
        AttributesWithTerminal newAttributes = this.getAttributes().withSlope(newSlope);
        checkAttributes(newAttributes);
        setSlopeOnCurrentVariant(newSlope);
        return this;
    }

    private void setSlopeOnCurrentVariant(double newSlope) {
        int currentVariantIndex = getCurrentVariantIndex();
        double oldSlope = this.slope.set(currentVariantIndex, newSlope);
        network.get().invalidateValidationLevel();
        String variantId = network.get().getVariantManager().getVariantId(currentVariantIndex);
        notifyUpdate(NotifyUpdateKey.SLOPE, variantId, oldSlope, newSlope);
    }

    @Override
    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public VoltageRegulation setTerminal(Terminal newTerminal, double newTargetValue) {
        if (this.network.get().getVariantManager().getVariantCount() > 1) {
            throw new PowsyblException(this.validable.getMessageHeader() + "Cannot set terminal when there are multiple variants");
        }
        AttributesWithTerminal newAttributes = this.getAttributes().withTerminalAndTargetValue(newTerminal, newTargetValue);
        checkAttributes(newAttributes);
        if (newTerminal == null) {
            ValidationUtil.checkLocalTargetQandV(validable,
                classHolder,
                this.holder.getLocalTargetV(),
                this.holder.getLocalTargetQ(),
                false,
                isRegulating(),
                false,
                getMode(),
                network.get().getMinValidationLevel(),
                network.get().getReportNodeContext().getReportNode());
        }
        this.updateTerminal(newTerminal);
        this.setTargetValueOnCurrentVariant(newTargetValue);
        return this;
    }

    @Override
    public @Nullable RegulationMode getMode() {
        int modeIndex = regulationMode.get(getCurrentVariantIndex());
        return UNDEFINED_REGULATION_MODE == modeIndex ? null : RegulationMode.fromIndex(modeIndex);
    }

    @Override
    public VoltageRegulation setMode(RegulationMode newMode) {
        AttributesWithTerminal newAttributes = getAttributes().withMode(newMode);
        checkAttributes(newAttributes);
        ValidationUtil.checkLocalTargetQandV(validable,
            classHolder,
            this.holder.getLocalTargetV(),
            this.holder.getLocalTargetQ(),
            false,
            isRegulating(),
            false,
            getMode(),
            network.get().getMinValidationLevel(),
            network.get().getReportNodeContext().getReportNode());
        setModeOnCurrentVariant(newMode);
        return this;
    }

    private void setModeOnCurrentVariant(RegulationMode newMode) {
        RegulationMode oldMode = getMode();
        int currentVariantIndex = getCurrentVariantIndex();
        regulationMode.set(currentVariantIndex, newMode != null ? newMode.getIndex() : UNDEFINED_REGULATION_MODE);
        network.get().invalidateValidationLevel();
        String variantId = network.get().getVariantManager().getVariantId(currentVariantIndex);
        notifyUpdate(NotifyUpdateKey.REGULATION_MODE, variantId, oldMode, newMode);
    }

    @Override
    public boolean isRegulating() {
        return regulating.get(getCurrentVariantIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true, then we validate all the attributes before setting regulating to true
     */
    @Override
    public VoltageRegulation setRegulating(boolean newRegulating) {
        ValidationUtil.checkLocalTargetQandV(validable,
            classHolder,
            this.holder.getLocalTargetV(),
            this.holder.getLocalTargetQ(),
            false,
            newRegulating,
            isWithTerminal(),
            getMode(),
            network.get().getMinValidationLevel(),
            network.get().getReportNodeContext().getReportNode());
        AttributesWithTerminal newAttributes = this.getAttributes().withRegulating(newRegulating);
        checkAttributes(newAttributes);
        if (holder instanceof RatioTapChanger ratioTapChanger) {
            ValidationUtil.checkRTCLoadTapChangingCapabilities(validable,
                ratioTapChanger.hasLoadTapChangingCapabilities(),
                newRegulating,
                network.get().getMinValidationLevel(),
                network.get().getReportNodeContext().getReportNode());
        }
        setRegulatingOnCurrentVariant(newRegulating);
        return this;
    }

    private void setRegulatingOnCurrentVariant(boolean newRegulating) {
        boolean oldRegulating = isRegulating();
        int currentVariantIndex = getCurrentVariantIndex();
        this.regulating.set(currentVariantIndex, newRegulating);
        network.get().invalidateValidationLevel();
        String variantId = network.get().getVariantManager().getVariantId(currentVariantIndex);
        notifyUpdate(NotifyUpdateKey.REGULATING, variantId, oldRegulating, newRegulating);
    }

    @Override
    public boolean isWithTerminal() {
        return terminal != null;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        targetValue.ensureCapacity(targetValue.size() + number);
        targetDeadband.ensureCapacity(targetDeadband.size() + number);
        slope.ensureCapacity(slope.size() + number);
        regulating.ensureCapacity(regulating.size() + number);
        regulationMode.ensureCapacity(regulationMode.size() + number);
        for (int i = 0; i < number; i++) {
            targetValue.add(targetValue.get(sourceIndex));
            targetDeadband.add(targetDeadband.get(sourceIndex));
            slope.add(slope.get(sourceIndex));
            regulating.add(regulating.get(sourceIndex));
            regulationMode.add(regulationMode.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        targetValue.remove(targetValue.size() - number, number);
        targetDeadband.remove(targetDeadband.size() - number, number);
        slope.remove(slope.size() - number, number);
        regulating.remove(regulating.size() - number, number);
        regulationMode.remove(regulationMode.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            targetValue.set(index, targetValue.get(sourceIndex));
            targetDeadband.set(index, targetDeadband.get(sourceIndex));
            slope.set(index, slope.get(sourceIndex));
            regulating.set(index, regulating.get(sourceIndex));
            regulationMode.set(index, regulationMode.get(sourceIndex));
        }
    }

    @Override
    public void onReferencedRemoval(Terminal removedReferenced) {
        if (this.terminal == removedReferenced) {
            this.actionOnRemovedTerminal();
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        if (this.terminal == oldReferenced) {
            this.updateTerminal(newReferenced);
        }
    }

    @Override
    public void onRemove() {
        if (this.terminal != null) {
            this.terminal.getReferrerManager().unregister(this);
        }
    }

    @Override
    public void setAttributesOnCurrentVariant(AttributesWithTerminal attributes) {
        checkAttributes(attributes);
        this.setModeOnCurrentVariant(attributes.mode());
        this.setSlopeOnCurrentVariant(attributes.slope());
        this.setTargetDeadbandOnCurrentVariant(attributes.targetDeadband());
        this.updateTerminal(attributes.terminal());
        this.setTargetValueOnCurrentVariant(attributes.targetValue());
        this.setRegulatingOnCurrentVariant(attributes.isRegulating());
    }

    private int getCurrentVariantIndex() {
        return network.get().getVariantIndex();
    }

    private void updateTerminal(Terminal newTerminal) {
        Terminal oldTerminal = this.terminal;
        if (this.terminal != null) {
            this.terminal.getReferrerManager().unregister(this);
            this.terminal = null;
        }
        if (newTerminal != null) {
            this.terminal = (TerminalExt) newTerminal;
            this.terminal.getReferrerManager().register(this);
        }
        network.get().invalidateValidationLevel();
        notifyUpdate(NotifyUpdateKey.TERMINAL, oldTerminal, newTerminal);
    }

    private void actionOnRemovedTerminal() {
        TerminalExt oldRegulatingTerminal = terminal;
        Terminal localTerminal = holder.getTerminal();
        String regulatedEquipmentId = getRegulatedEquipmentId(localTerminal);
        boolean updateTerminal = StaticVarCompensator.class != classHolder || holder.isWithMode(VOLTAGE);
        // if local voltage regulation, we keep the regulating status and re-locate the regulation at the regulated equipment
        if (localTerminal != null && updateTerminal) {
            Bus bus = oldRegulatingTerminal.getBusView().getBus();
            Bus localBus = localTerminal.getBusView().getBus();
            if (bus != null && bus == localBus) {
                LOGGER.warn("Connectable {} was a local voltage regulation point for {}. Regulation point is re-located at {}.", oldRegulatingTerminal.getConnectable().getId(),
                    regulatedEquipmentId, regulatedEquipmentId);
                updateTerminal(localTerminal);
                // TODO MSA update the targetValue? for each variants?
                return;
            }
        }
        updateTerminal(null);
        regulating.fill(0, regulating.size(), false);
        targetValue.fill(0, targetValue.size(), Double.NaN);
        regulationMode.fill(0, regulationMode.size(), VOLTAGE.getIndex());
        // TODO MSA add a default localTargetQ if missing in a variant?
        LOGGER.warn("Connectable {} was a regulation point for {}. Regulation is deactivated", oldRegulatingTerminal.getConnectable().getId(), regulatedEquipmentId);
    }

    private String getRegulatedEquipmentId(Terminal localTerminal) {
        return localTerminal != null ? localTerminal.getConnectable().getId() : getIdentifiable().getId();
    }

    protected void notifyUpdate(@NonNull NotifyUpdateKey attribute, Object oldValue, Object newValue) {
        network.get().getListeners().notifyUpdate(getIdentifiable(), attribute.getKey(), oldValue, newValue);
    }

    protected void notifyUpdate(@NonNull NotifyUpdateKey attribute, String variantId, Object oldValue, Object newValue) {
        network.get().getListeners().notifyUpdate(getIdentifiable(), attribute.getKey(), variantId, oldValue, newValue);
    }

    private Identifiable<?> getIdentifiable() {
        if (validable instanceof RatioTapChangerParent parent) {
            return parent.getTransformer();
        } else {
            return (Identifiable<?>) this.validable;
        }
    }
}
