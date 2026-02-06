/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.regulation;

import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationImpl implements VoltageRegulation, MultiVariantObject, Referrer<Terminal>, Validable {

    private TerminalExt terminal;
    private RegulationMode mode;
    private final Ref<NetworkImpl> network;
    // attributes depending on the variant
    private final TDoubleArrayList targetValue;
    private final TDoubleArrayList targetDeadband;
    private final TDoubleArrayList slope;
    private final TBooleanArrayList regulating;

    public VoltageRegulationImpl(Ref<NetworkImpl> network,
                                 Double targetValue,
                                 Double targetDeadband,
                                 Double slope,
                                 Terminal terminal,
                                 RegulationMode mode,
                                 boolean regulating) {
        this.network = network;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.setTerminal(terminal);
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
    }

    @Override
    public Double getTargetValue() {
        return targetValue.get(getCurrentIndex());
    }

    @Override
    public Double setTargetValue(Double targetValue) {
        return this.targetValue.set(getCurrentIndex(), targetValue);
    }

    @Override
    public Double getTargetDeadband() {
        return targetDeadband.get(getCurrentIndex());
    }

    @Override
    public Double setTargetDeadband(Double targetDeadband) {
        return this.targetDeadband.set(getCurrentIndex(), targetDeadband);
    }

    @Override
    public Double getSlope() {
        return slope.get(getCurrentIndex());
    }

    @Override
    public Double setSlope(Double slope) {
        return this.slope.set(getCurrentIndex(), slope);
    }

    @Override
    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public void setTerminal(Terminal terminal) {
        // TODO MSA add check terminal != localTerminal
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
    public RegulationMode getMode() {
        return mode;
    }

    @Override
    public void setMode(RegulationMode mode) {
        this.mode = mode;
    }

    @Override
    public Boolean isRegulating() {
        return regulating.get(getCurrentIndex());
    }

    @Override
    public Boolean setRegulating(Boolean regulating) {
        return this.regulating.set(getCurrentIndex(), regulating);
    }

    @Override
    public void remove() {
        if (terminal != null) {
            terminal.getReferrerManager().unregister(this);
        }
    }

    public static VoltageRegulationBuilderImpl newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl();
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
        // Nothing to do ?? TODO MSA check that
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
            this.setTerminal(null);
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        if (this.terminal == oldReferenced) {
            this.setTerminal(newReferenced);
        }
    }

    @Override
    public MessageHeader getMessageHeader() {
        return new DefaultMessageHeader("TYPE", "MSAID");
    }

    private double getTargetValue(RegulationMode expectedMode) {
        if (expectedMode.equals(mode)) {
            return this.getTargetValue();
        }
        return Double.NaN;
//        throw new IllegalArgumentException("Can't get targetValue value for RegulationMode : " + expectedMode + ", the RegulationMode must be " + this.mode);
    }

    public double getTargetV() {
        return this.getTargetValue(RegulationMode.VOLTAGE);
    }

    public void setTargetV(double targetV) {
        if (RegulationMode.VOLTAGE.equals(mode)) {
            this.targetValue.set(getCurrentIndex(), targetV);
        }
    }

    public double getTargetQ() {
        return this.getTargetValue(RegulationMode.REACTIVE_POWER);
    }

    public void setTargetQ(double targetQ) {
        if (RegulationMode.REACTIVE_POWER.equals(mode)) {
            this.targetValue.set(getCurrentIndex(), targetQ);
        }
    }

    public double getTargetQP() {
        return this.getTargetValue(RegulationMode.REACTIVE_POWER_PER_ACTIVE_POWER);
    }

    public double getTargetVQ() {
        return this.getTargetValue(RegulationMode.VOLTAGE_PER_REACTIVE_POWER);
    }

    private int getCurrentIndex() {
        return network.get().getVariantIndex();
    }
}
