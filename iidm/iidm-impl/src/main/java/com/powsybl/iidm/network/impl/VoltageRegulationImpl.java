/**
 * Copyright (c) 2025-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Bus;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationImpl implements VoltageRegulationExt {

    private static final Logger LOG = LoggerFactory.getLogger(VoltageRegulationImpl.class);

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
            this.setTerminal(attributes.terminal(), getTargetValue());
        }

    }

    private void initVariantAttributes(double targetValue, double targetDeadband, double slope, boolean regulating, RegulationMode mode, int variantArraySize) {
        Integer regulationModeIndex = RegulationMode.getIndex(mode);
        for (int i = 0; i < variantArraySize; i++) {
            // When the VoltageRegulation object is created and there's already other variants,
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

    @Override
    public double getTargetValue() {
        return targetValue.get(getCurrentVariantIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true then we validate the new value before to setting it
     */
    @Override
    public double setTargetValue(double targetValue) {
        ValidationUtil.checkVoltageRegulationTargetValue(validable, targetValue, getMode(), isRegulating(), isWithTerminal(), network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        return setTargetValueOnCurrentVariant(targetValue);
    }

    private double setTargetValueOnCurrentVariant(double targetValue) {
        return this.targetValue.set(getCurrentVariantIndex(), targetValue);
    }

    @Override
    public double getTargetDeadband() {
        return targetDeadband.get(getCurrentVariantIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true then we validate the new value before to setting it
     */
    @Override
    public double setTargetDeadband(double targetDeadband) {
        ValidationUtil.checkVoltageRegulationDeadband(validable, targetDeadband, isRegulating(), classHolder, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        return setTargetDeadbandOnCurrentVariant(targetDeadband);
    }

    private double setTargetDeadbandOnCurrentVariant(double targetDeadband) {
        return this.targetDeadband.set(getCurrentVariantIndex(), targetDeadband);
    }

    @Override
    public double getSlope() {
        return slope.get(getCurrentVariantIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true then we validate the new value before to setting it
     */
    @Override
    public double setSlope(double slope) {
        ValidationUtil.checkVoltageRegulationSlope(validable, slope, getMode(), isRegulating(), network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        return setSlopeOnCurrentVariant(slope);
    }

    private double setSlopeOnCurrentVariant(double slope) {
        return this.slope.set(getCurrentVariantIndex(), slope);
    }

    @Override
    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public void setTerminal(Terminal terminal, double targetValue) {
        ValidationUtil.checkVoltageRegulationTerminal(validable, terminal, isRegulating(), network.get(), network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        boolean isWithTerminal = terminal != null;
        ValidationUtil.checkVoltageRegulationTargetValue(validable, targetValue, getMode(), isRegulating(), isWithTerminal, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        this.updateTerminal(terminal);
        this.setTargetValue(targetValue);
    }

    @Override
    public @Nullable RegulationMode getMode() {
        int modeIndex = regulationMode.get(getCurrentVariantIndex());
        return UNDEFINED_REGULATION_MODE == modeIndex ? null : RegulationMode.fromIndex(modeIndex);
    }

    @Override
    public @Nullable RegulationMode setMode(RegulationMode mode) {
        ValidationUtil.checkVoltageRegulationMode(validable, mode, isRegulating(), isWithTerminal(), classHolder, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        // In any case we will check the voltageRegulation object with the new regulationMode value
        AttributesWithTerminal newAttributes = getAttributes().withMode(mode);
        ValidationUtil.checkVoltageRegulation(validable, newAttributes, isRegulating(), network.get(), classHolder, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        return setModeOnCurrentVariant(mode);
    }

    private @Nullable RegulationMode setModeOnCurrentVariant(RegulationMode mode) {
        RegulationMode oldMode = getMode();
        if (mode == null) {
            regulationMode.set(getCurrentVariantIndex(), UNDEFINED_REGULATION_MODE);
        } else {
            regulationMode.set(getCurrentVariantIndex(), mode.getIndex());
        }
        return oldMode;
    }

    @Override
    public boolean isRegulating() {
        return regulating.get(getCurrentVariantIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true then we validate all the attributes before setting regulating to true
     */
    @Override
    public boolean setRegulating(boolean regulating) {
        ValidationUtil.checkLocalTargetQandV(validable, classHolder, this.holder.getLocalTargetV(), this.holder.getLocalTargetQ(), false, regulating, isWithTerminal(), getMode(), network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        if (holder instanceof RatioTapChanger ratioTapChanger) {
            ValidationUtil.checkRTCLoadTapChangingCapabilities(validable, ratioTapChanger.hasLoadTapChangingCapabilities(), regulating, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        }
        // In any case we will check the voltageRegulation object with the new regulating value
        AttributesWithTerminal newAttributes = this.getAttributes().withRegulating(regulating);
        ValidationUtil.checkVoltageRegulation(validable, newAttributes, regulating, network.get(), classHolder, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        return setRegulatingOnCurrentVariant(regulating);
    }

    private boolean setRegulatingOnCurrentVariant(boolean regulating) {
        return this.regulating.set(getCurrentVariantIndex(), regulating);
    }

    @Override
    public void removeTerminal() {
        ValidationUtil.checkLocalTargetQandV(validable, classHolder, this.holder.getLocalTargetV(), this.holder.getLocalTargetQ(), false, isRegulating(), isWithTerminal(), getMode(), network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        this.updateTerminal(null);
        this.setTargetValue(Double.NaN);
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

    private void setAttributesOnCurrentVariant(AttributesWithTerminal attributes) {
        RegulationMode currentVariantMode = attributes.mode();
        boolean currentVariantRegulating = attributes.isRegulating();
        Terminal currentVariantTerminal = attributes.terminal();
        boolean currentVariantWithTerminal = currentVariantTerminal != null;
        double currentVariantTargetValue = attributes.targetValue();
        double currentVariantSlope = attributes.slope();
        double currentVariantTargetDeadband = attributes.targetDeadband();

        ValidationUtil.checkVoltageRegulationMode(validable, currentVariantMode, currentVariantRegulating, currentVariantWithTerminal, classHolder, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        ValidationUtil.checkVoltageRegulationSlope(validable, currentVariantSlope, currentVariantMode, currentVariantRegulating, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        ValidationUtil.checkVoltageRegulationDeadband(validable, currentVariantTargetDeadband, currentVariantRegulating, classHolder, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        ValidationUtil.checkVoltageRegulationTerminal(validable, currentVariantTerminal, currentVariantRegulating, network.get(), network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        ValidationUtil.checkVoltageRegulationTargetValue(validable, currentVariantTargetValue, currentVariantMode, currentVariantRegulating, currentVariantWithTerminal, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());

        this.setModeOnCurrentVariant(currentVariantMode);
        this.setSlopeOnCurrentVariant(currentVariantSlope);
        this.setTargetDeadbandOnCurrentVariant(currentVariantTargetDeadband);
        this.updateTerminal(currentVariantTerminal);
        this.setTargetValueOnCurrentVariant(currentVariantTargetValue);
        this.setRegulatingOnCurrentVariant(currentVariantRegulating);
    }

    @Override
    public void setAttributesOnCurrentVariant(VoltageRegulation voltageRegulation) {
        setAttributesOnCurrentVariant(new AttributesWithTerminal(
            new Attributes(voltageRegulation.getTargetValue(),
                voltageRegulation.getTargetDeadband(),
                voltageRegulation.getSlope(),
                voltageRegulation.getMode(),
                voltageRegulation.isRegulating()),
            voltageRegulation.getTerminal()));
    }

    private int getCurrentVariantIndex() {
        return network.get().getVariantIndex();
    }

    private void updateTerminal(Terminal terminal) {
        if (this.terminal != null) {
            this.terminal.getReferrerManager().unregister(this);
            this.terminal = null;
        }
        if (terminal != null) {
            this.terminal = (TerminalExt) terminal;
            this.terminal.getReferrerManager().register(this);
        }
    }

    private void actionOnRemovedTerminal() {
        TerminalExt oldRegulatingTerminal = terminal;
        Terminal localTerminal = holder.getTerminal();
        String regulatedEquipmentId = getRegulatedEquipmentId(localTerminal);
        boolean updateTerminal = StaticVarCompensator.class != classHolder || holder.isWithMode(RegulationMode.VOLTAGE);
        // if local voltage regulation, we keep the regulating status and re-locate the regulation at the regulated equipment
        if (localTerminal != null && updateTerminal) {
            Bus bus = terminal.getBusView().getBus();
            Bus localBus = localTerminal.getBusView().getBus();
            if (bus != null && bus == localBus) {
                LOG.warn("Connectable {} was a local voltage regulation point for {}. Regulation point is re-located at {}.", terminal.getConnectable().getId(),
                    regulatedEquipmentId, regulatedEquipmentId);
                updateTerminal(localTerminal);
                return;
            } else {
                updateTerminal(null);
            }
        } else {
            updateTerminal(null);
        }
        LOG.warn("Connectable {} was a regulation point for {}. Regulation is deactivated", oldRegulatingTerminal.getConnectable().getId(), regulatedEquipmentId);
        if (regulating != null) {
            regulating.fill(0, regulating.size(), false);
        }
        if (getMode() != null) {
            setMode(RegulationMode.VOLTAGE);
        }
    }

    private String getRegulatedEquipmentId(Terminal localTerminal) {
        String regulatedEquipmentId;
        if (localTerminal != null) {
            regulatedEquipmentId = localTerminal.getConnectable().getId();
        } else {
            if (validable instanceof RatioTapChangerParent parent) {
                regulatedEquipmentId = parent.getTransformer().getId();
            } else {
                regulatedEquipmentId = "";
            }
        }
        return regulatedEquipmentId;
    }
}
