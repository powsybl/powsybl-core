/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public class RemoteReactivePowerControlImpl extends AbstractMultiVariantIdentifiableExtension<Generator> implements RemoteReactivePowerControl {

    private TDoubleArrayList targetQ;

    private final ArrayList<Terminal> regulatingTerminal;

    private TBooleanArrayList enabled;

    public RemoteReactivePowerControlImpl(Generator generator, double targetQ, Terminal regulatingTerminal, boolean enabled) {
        super(generator);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.targetQ = new TDoubleArrayList();
        this.regulatingTerminal = new ArrayList<>();
        this.enabled = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.targetQ.add(targetQ);
            this.enabled.add(enabled);
            this.regulatingTerminal.add(regulatingTerminal);
        }
    }

    @Override
    public double getTargetQ() {
        return targetQ.get(getVariantIndex());
    }

    @Override
    public RemoteReactivePowerControl setTargetQ(double targetQ) {
        this.targetQ.set(getVariantIndex(), targetQ);
        return this;
    }

    @Override
    public RemoteReactivePowerControl setEnabled(boolean enabled) {
        this.enabled.set(getVariantIndex(), enabled);
        return null;
    }

    @Override
    public RemoteReactivePowerControl setRegulatingTerminal(Terminal terminal) {
        this.regulatingTerminal.set(getVariantIndex(), terminal);
        return null;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return regulatingTerminal.get(getVariantIndex());
    }

    @Override
    public boolean isEnabled() {
        return enabled.get(getVariantIndex());
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        regulatingTerminal.ensureCapacity(regulatingTerminal.size() + number);
        enabled.ensureCapacity(enabled.size() + number);
        targetQ.ensureCapacity(targetQ.size() + number);
        for (int i = 0; i < number; ++i) {
            regulatingTerminal.add(regulatingTerminal.get(sourceIndex));
            enabled.add(enabled.get(sourceIndex));
            targetQ.add(targetQ.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int i = 0; i < number; i++) {
            regulatingTerminal.remove(regulatingTerminal.size() - 1);
        }
        enabled.remove(enabled.size() - number, number);
        targetQ.remove(targetQ.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        regulatingTerminal.set(index, null);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            targetQ.set(index, targetQ.get(sourceIndex));
            enabled.set(index, enabled.get(sourceIndex));
            regulatingTerminal.set(index, regulatingTerminal.get(sourceIndex));
        }
    }
}
