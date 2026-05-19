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
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationImpl implements VoltageRegulationExt {

    private Validable validable;
    private VoltageRegulationHolder parent;
    private final Class<? extends VoltageRegulationHolder> classHolder;
    private TerminalExt terminal;
    private Terminal.TerminalDataMsa terminalData;
    private final RegulationMode mode;
    private final Ref<NetworkImpl> network;
    // attributes depending on the variant
    private final TDoubleArrayList targetValue;
    private final TDoubleArrayList targetDeadband;
    private final TDoubleArrayList slope;
    private final TBooleanArrayList regulating;

    public VoltageRegulationImpl(Validable validable,
                                 VoltageRegulationHolder parent,
                                 Class<? extends VoltageRegulationHolder> classHolder,
                                 Ref<NetworkImpl> network,
                                 double targetValue,
                                 double targetDeadband,
                                 double slope,
                                 Terminal terminal,
                                 Terminal.TerminalDataMsa terminalData,
                                 RegulationMode mode,
                                 boolean regulating) {
        this.validable = validable;
        this.parent = parent;
        this.classHolder = classHolder;
        this.network = network;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.mode = mode;
        this.targetValue = new TDoubleArrayList(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        this.slope = new TDoubleArrayList(variantArraySize);
        this.regulating = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.targetValue.add(targetValue);
            this.targetDeadband.add(targetDeadband);
            this.slope.add(slope);
            this.regulating.add(regulating);
        }
        if (terminal != null) {
            this.setTerminal(terminal, getTargetValue());
        }
        this.terminalData = terminalData;
    }

    @Override
    public double getTargetValue() {
        return targetValue.get(getCurrentIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true then we validate the new value before to setting it
     */
    @Override
    public double setTargetValue(double targetValue) {
        ValidationUtil.checkVoltageRegulationTargetValue(validable, targetValue, mode, isRegulating(), isWithTerminal(), network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        return this.targetValue.set(getCurrentIndex(), targetValue);
    }

    @Override
    public double getTargetDeadband() {
        return targetDeadband.get(getCurrentIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true then we validate the new value before to setting it
     */
    @Override
    public double setTargetDeadband(double targetDeadband) {
        ValidationUtil.checkVoltageRegulationDeadband(validable, targetDeadband, isRegulating(), classHolder, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        return this.targetDeadband.set(getCurrentIndex(), targetDeadband);
    }

    @Override
    public double getSlope() {
        return slope.get(getCurrentIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true then we validate the new value before to setting it
     */
    @Override
    public double setSlope(double slope) {
        ValidationUtil.checkVoltageRegulationSlope(validable, slope, mode, isRegulating(), network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        return this.slope.set(getCurrentIndex(), slope);
    }

    @Override
    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public void setTerminal(Terminal terminal, double targetValue) {
        ValidationUtil.checkVoltageRegulationTerminal(validable, terminal, isRegulating(), network.get(), network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        boolean isWithTerminal = terminal != null;
        ValidationUtil.checkVoltageRegulationTargetValue(validable, targetValue, mode, isRegulating(), isWithTerminal, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        this.updateTerminal(terminal);
        this.setTargetValue(targetValue);
    }

    @Override
    public void resolveTerminal() {
        if (terminalData != null) {
            this.setTerminal(Terminal.getTerminal(this.terminalData, this.network.get()), this.getTargetValue());
            this.terminalData = null;
        }
    }

    @Override
    public RegulationMode getMode() {
        return mode;
    }

    @Override
    public boolean isRegulating() {
        return regulating.get(getCurrentIndex());
    }

    /**
     * {@inheritDoc}
     * If regulating is true then we validate all the attributes before setting regulating to true
     */
    @Override
    public boolean setRegulating(boolean regulating) {
        if (!isWithTerminal()) {
            if (classHolder == ShuntCompensator.class) {
                if (regulating) {
                    ValidationUtil.checkNotNanValue(validable, this.parent.getLocalTargetV(), "localTargetV", null);
                    ValidationUtil.checkDoublePositive(validable, this.parent.getLocalTargetV(), "localTargetV");
                }
                // StaticVarCompensator ignore localTarget V and Q when regulating false
            } else if (classHolder != StaticVarCompensator.class || regulating) {
                ValidationUtil.checkLocalTargetQandV(validable, this.parent.getLocalTargetV(), this.parent.getLocalTargetQ(), false, regulating, mode, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
            }
        }
        ValidationUtil.checkVoltageRegulation(validable, this, regulating, network.get(), classHolder, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        return this.regulating.set(getCurrentIndex(), regulating);
    }

    @Override
    public void removeTerminal() {
        // No exception on ShuntCompensator case
        if (classHolder != ShuntCompensator.class) {
            ValidationUtil.checkLocalTargetQandV(validable, this.parent.getLocalTargetV(), this.parent.getLocalTargetQ(), false, isRegulating(), mode, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode());
        }
        this.updateTerminal(null);
        this.setTargetValue(Double.NaN);
    }

    @Override
    public boolean isWithTerminal() {
        return terminal != null || terminalData != null;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        targetValue.ensureCapacity(targetValue.size() + number);
        targetDeadband.ensureCapacity(targetDeadband.size() + number);
        slope.ensureCapacity(slope.size() + number);
        regulating.ensureCapacity(regulating.size() + number);
        for (int i = 0; i < number; i++) {
            targetValue.add(targetValue.get(sourceIndex));
            targetDeadband.add(targetDeadband.get(sourceIndex));
            slope.add(slope.get(sourceIndex));
            regulating.add(regulating.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        targetValue.remove(targetValue.size() - number, number);
        targetDeadband.remove(targetDeadband.size() - number, number);
        slope.remove(slope.size() - number, number);
        regulating.remove(regulating.size() - number, number);
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
        }
    }

    @Override
    public void onReferencedRemoval(Terminal removedReferenced) {
        if (this.terminal == removedReferenced) {
            this.updateTerminal(null);
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        if (this.terminal == oldReferenced) {
            this.updateTerminal(newReferenced);
        }
    }

    @Override
    public void updateValidable(Validable validable) {
        this.validable = validable;
    }

    @Override
    public void setParent(VoltageRegulationHolder parent) {
        this.parent = parent;
    }

    private int getCurrentIndex() {
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

    @Override
    public void remove() {
        if (this.terminal != null) {
            this.terminal.getReferrerManager().unregister(this);
        }
    }
}
