/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class VoltageRegulationImpl extends AbstractMultiVariantIdentifiableExtension<Battery> implements VoltageRegulation {

    private final TBooleanArrayList voltageRegulatorOn;

    private final TDoubleArrayList targetV;

    private Terminal regulatingTerminal;

    public VoltageRegulationImpl(Battery battery, Terminal regulatingTerminal, Boolean voltageRegulatorOn, double targetV) {
        super(battery);
        checkRegulatingTerminal(regulatingTerminal, getNetworkFromExtendable());
        if (voltageRegulatorOn == null) {
            throw new PowsyblException("Voltage regulator status is not defined");
        }
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.setRegulatingTerminal(regulatingTerminal);
        this.voltageRegulatorOn = new TBooleanArrayList(variantArraySize);
        this.targetV = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.voltageRegulatorOn.add(voltageRegulatorOn);
            this.targetV.add(targetV);
        }
    }

    private static void checkRegulatingTerminal(Terminal regulatingTerminal, Network network) {
        if (regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() != network) {
            throw new PowsyblException("regulating terminal is not part of the same network");
        }
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return regulatingTerminal;
    }

    @Override
    public void setRegulatingTerminal(Terminal regulatingTerminal) {
        checkRegulatingTerminal(regulatingTerminal, getNetworkFromExtendable());
        this.regulatingTerminal = regulatingTerminal != null ? regulatingTerminal : getExtendable().getTerminal();
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return voltageRegulatorOn.get(getVariantIndex());
    }

    @Override
    public void setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn.set(getVariantIndex(), voltageRegulatorOn);
    }

    @Override
    public double getTargetV() {
        return targetV.get(getVariantIndex());
    }

    @Override
    public void setTargetV(double targetV) {
        this.targetV.set(getVariantIndex(), targetV);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        voltageRegulatorOn.ensureCapacity(voltageRegulatorOn.size() + number);
        targetV.ensureCapacity(targetV.size() + number);
        for (int i = 0; i < number; ++i) {
            voltageRegulatorOn.add(voltageRegulatorOn.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        voltageRegulatorOn.remove(voltageRegulatorOn.size() - number, number);
        targetV.remove(targetV.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            voltageRegulatorOn.set(index, voltageRegulatorOn.get(sourceIndex));
            targetV.set(index, targetV.get(sourceIndex));
        }
    }

    private Network getNetworkFromExtendable() {
        return getExtendable().getTerminal().getVoltageLevel().getNetwork();
    }
}
