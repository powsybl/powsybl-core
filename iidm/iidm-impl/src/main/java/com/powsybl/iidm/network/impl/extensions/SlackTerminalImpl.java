/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import com.powsybl.iidm.network.impl.TerminalExt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class SlackTerminalImpl extends AbstractMultiVariantIdentifiableExtension<VoltageLevel> implements SlackTerminal {

    private final ArrayList<Terminal> terminals;

    SlackTerminalImpl(VoltageLevel voltageLevel, Terminal terminal) {
        super(voltageLevel);
        this.terminals = new ArrayList<>(
            Collections.nCopies(getVariantManagerHolder().getVariantManager().getVariantArraySize(), null));
        this.setTerminal(terminal);
    }

    private void unregisterReferencedTerminalIfNeeded(int variantIndex) {
        // check there is no more same terminal referenced by any variant, unregister it
        Terminal oldTerminal = terminals.get(variantIndex);
        if (oldTerminal != null && !terminals.contains(oldTerminal)) {
            ((TerminalExt) oldTerminal).unregisterDependent(this);
        }
    }

    private void registerReferencedTerminalIfNeeded(Terminal terminal) {
        // if terminal was not already referenced by another variant, register it
        if (terminal != null && !terminals.contains(terminal)) {
            ((TerminalExt) terminal).registerDependent(this);
        }
    }

    private void setTerminalAndUpdateReferences(int variantIndex, Terminal terminal) {
        unregisterReferencedTerminalIfNeeded(variantIndex);
        registerReferencedTerminalIfNeeded(terminal);
        terminals.set(variantIndex, terminal);
    }

    private void addTerminalAndUpdateReferences(Terminal terminal) {
        registerReferencedTerminalIfNeeded(terminal);
        terminals.add(terminal);
    }

    private void removeTerminalAndUpdateReferences(int variantIndex) {
        unregisterReferencedTerminalIfNeeded(variantIndex);
        terminals.remove(variantIndex);
    }

    @Override
    public Terminal getTerminal() {
        return terminals.get(getVariantIndex());
    }

    @Override
    public SlackTerminal setTerminal(Terminal terminal) {
        if (terminal != null && !terminal.getVoltageLevel().equals(getExtendable())) {
            throw new PowsyblException("Terminal given is not in the right VoltageLevel ("
                + terminal.getVoltageLevel().getId() + " instead of " + getExtendable().getId() + ")");
        }
        setTerminalAndUpdateReferences(getVariantIndex(), terminal);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return terminals.stream().noneMatch(Objects::nonNull);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        terminals.ensureCapacity(terminals.size() + number);
        Terminal sourceTerminal = terminals.get(sourceIndex);
        for (int i = 0; i < number; ++i) {
            addTerminalAndUpdateReferences(sourceTerminal);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int i = 0; i < number; i++) {
            removeTerminalAndUpdateReferences(terminals.size() - 1); // remove elements from the top to avoid moves inside the array
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        setTerminalAndUpdateReferences(index, null);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        Terminal terminalSource = terminals.get(sourceIndex);
        for (int index : indexes) {
            setTerminalAndUpdateReferences(index, terminalSource);
        }
    }

    @Override
    public void onReferencedRemoval(Terminal terminal) {
        int variantIndex = terminals.indexOf(terminal);
        if (variantIndex != -1) {
            terminals.set(variantIndex, null);
        }
    }
}
