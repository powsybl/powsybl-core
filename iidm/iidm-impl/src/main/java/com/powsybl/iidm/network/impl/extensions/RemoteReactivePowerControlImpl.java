/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.fastutil.ExtendedBooleanArrayList;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import com.powsybl.iidm.network.impl.TerminalExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class RemoteReactivePowerControlImpl extends AbstractMultiVariantIdentifiableExtension<Generator> implements RemoteReactivePowerControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteReactivePowerControlImpl.class);

    private final ExtendedDoubleArrayList targetQ;

    private Terminal regulatingTerminal;

    private final ExtendedBooleanArrayList enabled;

    public RemoteReactivePowerControlImpl(Generator generator, double targetQ, Terminal regulatingTerminal, boolean enabled) {
        super(generator);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.targetQ = new ExtendedDoubleArrayList(variantArraySize, targetQ);
        this.regulatingTerminal = Objects.requireNonNull(regulatingTerminal);
        this.enabled = new ExtendedBooleanArrayList(variantArraySize, enabled);
        if (regulatingTerminal.getVoltageLevel().getParentNetwork() != getExtendable().getParentNetwork()) {
            throw new PowsyblException("Regulating terminal is not in the right Network ("
                    + regulatingTerminal.getVoltageLevel().getParentNetwork().getId() + " instead of "
                    + getExtendable().getParentNetwork().getId() + ")");
        }
        ((TerminalExt) regulatingTerminal).getReferrerManager().register(this);
    }

    @Override
    public double getTargetQ() {
        return targetQ.getDouble(getVariantIndex());
    }

    @Override
    public RemoteReactivePowerControl setTargetQ(double targetQ) {
        this.targetQ.set(getVariantIndex(), targetQ);
        return this;
    }

    @Override
    public RemoteReactivePowerControl setEnabled(boolean enabled) {
        this.enabled.set(getVariantIndex(), enabled);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return regulatingTerminal;
    }

    @Override
    public RemoteReactivePowerControl setRegulatingTerminal(Terminal regulatingTerminal) {
        Objects.requireNonNull(regulatingTerminal);
        if (this.regulatingTerminal != regulatingTerminal) {
            ((TerminalExt) this.regulatingTerminal).getReferrerManager().unregister(this);
            checkRegulatingTerminal(regulatingTerminal, getExtendable().getTerminal().getVoltageLevel().getNetwork());
            this.regulatingTerminal = regulatingTerminal;
            ((TerminalExt) regulatingTerminal).getReferrerManager().register(this);
        }
        return this;
    }

    private static void checkRegulatingTerminal(Terminal regulatingTerminal, Network network) {
        if (regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() != network) {
            throw new PowsyblException("regulating terminal is not part of the same network");
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled.getBoolean(getVariantIndex());
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        targetQ.growAndFill(number, targetQ.getDouble(sourceIndex));
        enabled.growAndFill(number, enabled.getBoolean(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        enabled.removeElements(number);
        targetQ.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            targetQ.set(index, targetQ.getDouble(sourceIndex));
            enabled.set(index, enabled.getBoolean(sourceIndex));
        }
    }

    @Override
    public void onReferencedRemoval(Terminal removedTerminal) {
        // we cannot set regulating terminal to null because otherwise extension won't be consistent anymore
        // we cannot also as for voltage regulation fallback to a local terminal
        // so we just remove the extension
        LOGGER.warn("Remove 'RemoteReactivePowerControl' extension of generator '{}', because its regulating terminal has been removed",
                getExtendable().getId());
        getExtendable().removeExtension(RemoteReactivePowerControl.class);
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
