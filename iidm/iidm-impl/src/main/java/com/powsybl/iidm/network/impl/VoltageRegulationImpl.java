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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationImpl implements VoltageRegulation, MultiVariantObject, Referrer<Terminal> {

    private TerminalExt terminal;
    private RegulationMode mode;
    private final Ref<? extends VariantManagerHolder> network;
    // attributes depending on the variant
    private final TDoubleArrayList targetValue;
    private final TDoubleArrayList targetDeadband;
    private final TDoubleArrayList slope;
    private final TBooleanArrayList regulating;

    public VoltageRegulationImpl(Ref<NetworkImpl> network,
                                 double targetValue,
                                 double targetDeadband,
                                 double slope,
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
    public double getTargetValue() {
        return targetValue.get(getCurrentIndex());
    }

    @Override
    public double setTargetValue(double targetValue) {
        // TODO MSA validation : use old validation methods
        return this.targetValue.set(getCurrentIndex(), targetValue);
    }

    @Override
    public double getTargetDeadband() {
        return targetDeadband.get(getCurrentIndex());
    }

    @Override
    public double setTargetDeadband(double targetDeadband) {
        // TODO MSA validation : use old validation methods
        return this.targetDeadband.set(getCurrentIndex(), targetDeadband);
    }

    @Override
    public double getSlope() {
        return slope.get(getCurrentIndex());
    }

    @Override
    public double setSlope(double slope) {
        // TODO MSA validation : use old validation methods
        return this.slope.set(getCurrentIndex(), slope);
    }

    @Override
    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public void setTerminal(Terminal terminal) {
        // TODO MSA validation : use old validation methods
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
    public boolean isRegulating() {
        return regulating.get(getCurrentIndex());
    }

    @Override
    public boolean setRegulating(boolean regulating) {
        return this.regulating.set(getCurrentIndex(), regulating);
    }

    @Override
    public void removeTerminal() {
        this.setTerminal(null);
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
            this.setTerminal(null);
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        if (this.terminal == oldReferenced) {
            this.setTerminal(newReferenced);
        }
    }

    private int getCurrentIndex() {
        return network.get().getVariantIndex();
    }
}
