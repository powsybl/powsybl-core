/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;

import java.util.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ReferenceTerminalsImpl extends AbstractMultiVariantIdentifiableExtension<Network> implements ReferenceTerminals {

    private final ArrayList<Set<Terminal>> terminalsPerVariant;

    public ReferenceTerminalsImpl(Network network, Set<Terminal> terminals) {
        super(network);
        this.terminalsPerVariant = new ArrayList<>(
                Collections.nCopies(getVariantManagerHolder().getVariantManager().getVariantArraySize(), new LinkedHashSet<>()));
        setReferenceTerminals(terminals);
    }

    private void unregisterReferencedTerminalIfNeeded(int variantIndex) {
        // check there is no more same terminal referenced by any variant, unregister it
        Set<Terminal> oldTerminals = terminalsPerVariant.get(variantIndex);
        if (oldTerminals != null) {
            for (Terminal oldTerminal : oldTerminals) {
                if (terminalsPerVariant.stream()
                    .flatMap(Collection::stream)
                    .noneMatch(t -> t == oldTerminal)) {
                    unregisterReferencedTerminal(oldTerminal);
                }
            }
        }
    }

    private void registerReferencedTerminalIfNeeded(Set<Terminal> terminals) {
        // if terminal was not already referenced by another variant, register it
        if (terminals != null) {
            for (Terminal terminal : terminals) {
                if (terminalsPerVariant.stream()
                        .flatMap(Collection::stream)
                        .noneMatch(t -> t == terminal)) {
                    registerReferencedTerminal(terminal);
                }
            }
        }
    }

    private void setTerminalsAndUpdateReferences(int variantIndex, Set<Terminal> terminals) {
        unregisterReferencedTerminalIfNeeded(variantIndex);
        registerReferencedTerminalIfNeeded(terminals);
        terminalsPerVariant.set(variantIndex, new LinkedHashSet<>(terminals));
    }

    private void addTerminalsAndUpdateReferences(Set<Terminal> terminals) {
        registerReferencedTerminalIfNeeded(terminals);
        terminalsPerVariant.add(new LinkedHashSet<>(terminals));
    }

    private void removeTerminalsAndUpdateReferences(int variantIndex) {
        unregisterReferencedTerminalIfNeeded(variantIndex);
        terminalsPerVariant.remove(variantIndex); // remove elements from the top to avoid moves inside the array
    }

    @Override
    public Set<Terminal> getReferenceTerminals() {
        return ImmutableSet.copyOf(terminalsPerVariant.get(getVariantIndex()));
    }

    @Override
    public void setReferenceTerminals(Set<Terminal> terminals) {
        Objects.requireNonNull(terminals);
        terminals.forEach(t -> checkTerminalInNetwork(t, getExtendable()));
        setTerminalsAndUpdateReferences(getVariantIndex(), terminals);
    }

    @Override
    public ReferenceTerminals reset() {
        setTerminalsAndUpdateReferences(getVariantIndex(), Collections.emptySet());
        return this;
    }

    @Override
    public ReferenceTerminals addReferenceTerminal(Terminal terminal) {
        Objects.requireNonNull(terminal);
        checkTerminalInNetwork(terminal, getExtendable());
        terminalsPerVariant.get(getVariantIndex()).add(terminal);
        return this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        terminalsPerVariant.ensureCapacity(terminalsPerVariant.size() + number);
        Set<Terminal> sourceTerminals = terminalsPerVariant.get(sourceIndex);
        for (int i = 0; i < number; ++i) {
            addTerminalsAndUpdateReferences(sourceTerminals);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int i = 0; i < number; i++) {
            removeTerminalsAndUpdateReferences(terminalsPerVariant.size() - 1); // remove elements from the top to avoid moves inside the array
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        setTerminalsAndUpdateReferences(index, Collections.emptySet());
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        Set<Terminal> sourceTerminals = terminalsPerVariant.get(sourceIndex);
        for (int index : indexes) {
            setTerminalsAndUpdateReferences(index, sourceTerminals);
        }
    }

    private static void checkTerminalInNetwork(Terminal terminal, Network network) {
        final boolean extendableIsRootNetwork = network.getNetwork().equals(network);
        if (extendableIsRootNetwork) {
            // it is all fine as long as the terminal belongs to the merged network
            if (!terminal.getVoltageLevel().getNetwork().equals(network)) {
                throw new PowsyblException("Terminal given is not in the right Network ("
                        + terminal.getVoltageLevel().getNetwork().getId() + " instead of " + network.getId() + ")");
            }
        } else {
            // subnetwork: the terminal must be in the subnetwork
            if (!terminal.getVoltageLevel().getParentNetwork().equals(network)) {
                throw new PowsyblException("Terminal given is not in the right Network ("
                        + terminal.getVoltageLevel().getParentNetwork().getId() + " instead of " + network.getId() + ")");
            }
        }
    }

    @Override
    public void onReferencedTerminalRemoval(Terminal terminal) {
        for (Set<Terminal> terminals : terminalsPerVariant) {
            terminals.remove(terminal);
        }
    }
}
