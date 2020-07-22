/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;

import java.util.ArrayList;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SlackTerminalImpl extends AbstractMultiVariantIdentifiableExtension<VoltageLevel> implements SlackTerminal {

    private final ArrayList<Terminal> terminals;

    SlackTerminalImpl(VoltageLevel voltageLevel, Terminal terminal) {
        super(voltageLevel);

        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.terminals = new ArrayList<>(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.terminals.add(terminal);
        }
    }

    @Override
    public Terminal getTerminal() {
        return terminals.get(getVariantIndex());
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        terminals.ensureCapacity(terminals.size() + number);
        for (int i = 0; i < number; ++i) {
            terminals.add(terminals.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int i = 0; i < number; i++) {
            terminals.remove(terminals.size() - 1); // remove elements from the top to avoid moves inside the array
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        terminals.set(index, null);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        Terminal terminalSource = terminals.get(sourceIndex);
        for (int index : indexes) {
            terminals.set(index, terminalSource);
        }
    }
}
