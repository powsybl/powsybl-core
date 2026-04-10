/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.fastutil.ExtendedBooleanArrayList;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import com.powsybl.iidm.network.impl.TerminalExt;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class VoltageRegulationImpl extends AbstractMultiVariantIdentifiableExtension<Battery> implements VoltageRegulation {

    private final ExtendedBooleanArrayList voltageRegulatorOn;

    private final ExtendedDoubleArrayList targetV;

    private Terminal regulatingTerminal;

    public VoltageRegulationImpl(Battery battery, Terminal regulatingTerminal, Boolean voltageRegulatorOn, double targetV) {
        super(battery);
        checkRegulatingTerminal(regulatingTerminal, getNetworkFromExtendable());
        if (voltageRegulatorOn == null) {
            throw new PowsyblException("Voltage regulator status is not defined");
        }
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        setRegulatingTerminal(regulatingTerminal);
        this.voltageRegulatorOn = new ExtendedBooleanArrayList(variantArraySize, voltageRegulatorOn);
        this.targetV = new ExtendedDoubleArrayList(variantArraySize, targetV);
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
        Terminal newRegulatingTerminal = regulatingTerminal != null ? regulatingTerminal : getExtendable().getTerminal();
        if (newRegulatingTerminal != this.regulatingTerminal) {
            if (this.regulatingTerminal != null) {
                ((TerminalExt) this.regulatingTerminal).getReferrerManager().unregister(this);
            }
            this.regulatingTerminal = newRegulatingTerminal;
            ((TerminalExt) this.regulatingTerminal).getReferrerManager().register(this);
        }
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return voltageRegulatorOn.getBoolean(getVariantIndex());
    }

    @Override
    public void setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn.set(getVariantIndex(), voltageRegulatorOn);
    }

    @Override
    public double getTargetV() {
        return targetV.getDouble(getVariantIndex());
    }

    @Override
    public void setTargetV(double targetV) {
        this.targetV.set(getVariantIndex(), targetV);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        voltageRegulatorOn.growAndFill(number, voltageRegulatorOn.getBoolean(sourceIndex));
        targetV.growAndFill(number, targetV.getDouble(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        voltageRegulatorOn.removeElements(number);
        targetV.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            voltageRegulatorOn.set(index, voltageRegulatorOn.getBoolean(sourceIndex));
            targetV.set(index, targetV.getDouble(sourceIndex));
        }
    }

    private Network getNetworkFromExtendable() {
        return getExtendable().getTerminal().getVoltageLevel().getNetwork();
    }

    @Override
    public void onReferencedRemoval(Terminal removedTerminal) {
        if (regulatingTerminal == removedTerminal) {
            setRegulatingTerminal(null);
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        if (regulatingTerminal == oldReferenced) {
            setRegulatingTerminal(newReferenced);
        }
    }

    @Override
    public void cleanup() {
        if (regulatingTerminal != null) {
            ((TerminalExt) regulatingTerminal).getReferrerManager().unregister(this);
        }
    }
}
