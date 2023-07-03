/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.TerminalWithPriority;
import com.powsybl.iidm.network.extensions.TerminalWithPriorityImpl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;

import java.util.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SlackTerminalImpl extends AbstractMultiVariantIdentifiableExtension<VoltageLevel> implements SlackTerminal {

    private final ArrayList<ArrayList<TerminalWithPriority>> terminals;

    SlackTerminalImpl(VoltageLevel voltageLevel, ArrayList<? extends TerminalWithPriority> terminals) {
        super(voltageLevel);
        this.terminals = new ArrayList<>(
            Collections.nCopies(getVariantManagerHolder().getVariantManager().getVariantArraySize(), new ArrayList<>()));
        this.setTerminals(terminals);
    }

    @Override
    public List<TerminalWithPriority> getTerminals() {
        return terminals.get(getVariantIndex());
    }

    @Override
    public Terminal getTerminal() {
        List<TerminalWithPriority> variantTerminals = getTerminals();
        if (variantTerminals == null || variantTerminals.isEmpty()) {
            return null;
        }

        for (TerminalWithPriority terminalWithPriority : variantTerminals) {
            if (terminalWithPriority.getTerminal().isConnected()) {
                return terminalWithPriority.getTerminal();
            }
        }
        return variantTerminals.get(0).getTerminal();
    }

    @Override
    public SlackTerminal setTerminals(List<? extends TerminalWithPriority> terminals) {
        Objects.requireNonNull(terminals);

        for (TerminalWithPriority terminal : terminals) {
            if (terminal != null && !terminal.getVoltageLevel().equals(getExtendable())) {
                throw new PowsyblException("Terminal given is not in the right VoltageLevel ("
                    + terminal.getVoltageLevel().getId() + " instead of " + getExtendable().getId() + ")");
            }
        }
        this.terminals.set(getVariantIndex(), new ArrayList<>(terminals));
        return this;
    }

    public SlackTerminal setNoTerminal() {
        this.terminals.set(getVariantIndex(), new ArrayList<>());
        return this;
    }

    @Override
    public SlackTerminal setTerminal(Terminal terminal) {
        if (terminal == null) {
            return setNoTerminal();
        }
        return this.setTerminals(Arrays.asList(new TerminalWithPriorityImpl(terminal, 1)));
    }

    @Override
    public boolean isEmpty() {
        return terminals.stream().allMatch(List::isEmpty);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        terminals.ensureCapacity(terminals.size() + number);
        ArrayList<? extends TerminalWithPriority> sourceTerminals = terminals.get(sourceIndex);
        for (int i = 0; i < number; ++i) {
            terminals.add(new ArrayList<>(sourceTerminals));
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
        ArrayList<TerminalWithPriority> sourceTerminals = terminals.get(sourceIndex);
        for (int index : indexes) {
            terminals.set(index, sourceTerminals);
        }
    }
}
