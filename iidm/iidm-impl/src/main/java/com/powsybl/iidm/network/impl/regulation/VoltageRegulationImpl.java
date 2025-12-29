/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.regulation;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.impl.MultiVariantObject;
import com.powsybl.iidm.network.impl.Referrer;
import com.powsybl.iidm.network.impl.TerminalExt;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationImpl implements VoltageRegulation, MultiVariantObject, Referrer<Terminal> {

    private TerminalExt terminal;
    private RegulationMode mode;
    // attributes depending on the variant
    private final TDoubleArrayList targetValue;
    private final TDoubleArrayList targetDeadband;
    private final TDoubleArrayList slope;
    private final TBooleanArrayList regulating;

    public VoltageRegulationImpl(Double targetValue,
                                 Double targetDeadband,
                                 Double slope,
                                 Terminal terminal,
                                 RegulationMode mode,
                                 boolean regulating,
                                 int variantArraySize) {
        this.terminal = (TerminalExt) terminal;
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

    public double getTargetValue(int index) {
        return targetValue.get(index);
    }

    public double setTargetValue(double targetValue, int index) {
        return this.targetValue.set(index, targetValue);
    }

    public double getTargetDeadband(int index) {
        return targetDeadband.get(index);
    }

    public double setTargetDeadband(double targetDeadband, int index) {
        return this.targetDeadband.set(index, targetDeadband);
    }

    public double getSlope(int index) {
        return slope.get(index);
    }

    public double setSlope(double slope, int index) {
        return this.slope.set(index, slope);
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        if (this.terminal != null) {
            this.terminal.getReferrerManager().unregister(this);
            this.terminal = null;
        }
        if (terminal != null) {
            this.terminal = (TerminalExt) terminal;
            this.terminal.getReferrerManager().register(this);
        }
    }

    public RegulationMode getMode() {
        return mode;
    }

    public void setMode(RegulationMode mode) {
        this.mode = mode;
    }

    public boolean isRegulating(int index) {
        return regulating.get(index);
    }

    public boolean setRegulating(boolean regulating, int index) {
        return this.regulating.set(index, regulating);
    }

    @Override
    public void remove() {
        if (terminal != null) {
            terminal.getReferrerManager().unregister(this);
        }
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
            this.terminal = null;
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        if (this.terminal == oldReferenced) {
            this.terminal = (TerminalExt) newReferenced;
        }
    }

    public static class Builder {
        private Double targetValue = Double.NaN;
        private Double targetDeadband = Double.NaN;
        private Double slope = Double.NaN;
        private Terminal terminal = null;
        private RegulationMode mode = null;
        private boolean regulating = false;
        private int variantArraySize = 1;

        public Builder setTargetValue(Double targetValue) {
            this.targetValue = targetValue;
            return this;
        }

        public Builder setTargetDeadband(Double targetDeadband) {
            this.targetDeadband = targetDeadband;
            return this;
        }

        public Builder setSlope(Double slope) {
            this.slope = slope;
            return this;
        }

        public Builder setTerminal(Terminal terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder setMode(RegulationMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder setRegulating(boolean regulating) {
            this.regulating = regulating;
            return this;
        }

        public Builder setVariantArraySize(int variantArraySize) {
            this.variantArraySize = variantArraySize;
            return this;
        }

        public VoltageRegulationImpl build() {
            if (mode == null) {
//                throw new IllegalArgumentException("Invalid VoltageRegulation: mode must be defined.");
            }
            if (terminal == null) {
//                throw new IllegalArgumentException("Invalid VoltageRegulation: terminal must be defined.");
            }
            return new VoltageRegulationImpl(targetValue, targetDeadband, slope, terminal, mode, regulating, variantArraySize);
        }

    }

    public static Builder builder() {
        return new Builder();
    }
}
