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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;

import java.util.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ReferenceTerminalsImpl extends AbstractMultiVariantIdentifiableExtension<Network> implements ReferenceTerminals {

    private final class ReferenceTerminalsListener extends DefaultNetworkListener {
        @Override
        public void beforeRemoval(Identifiable identifiable) {
            if (identifiable instanceof Connectable<?> connectable) {
                // if connectable removed from network, remove its terminals from this extension
                terminalsPerVariant.forEach(referenceTerminals -> connectable.getTerminals().forEach(referenceTerminals::remove));
            }
        }
    }

    private final NetworkListener referenceTerminalsListener;
    private final ArrayList<Set<Terminal>> terminalsPerVariant;

    public ReferenceTerminalsImpl(Network network, Set<Terminal> terminals) {
        super(network);
        this.terminalsPerVariant = new ArrayList<>(
                Collections.nCopies(getVariantManagerHolder().getVariantManager().getVariantArraySize(), null));
        setReferenceTerminals(terminals);
        this.referenceTerminalsListener = new ReferenceTerminalsListener();
        network.addListener(this.referenceTerminalsListener);
    }

    @Override
    protected void cleanup() {
        getExtendable().removeListener(this.referenceTerminalsListener);
    }

    @Override
    public Set<Terminal> getReferenceTerminals() {
        return ImmutableSet.copyOf(terminalsPerVariant.get(getVariantIndex()));
    }

    @Override
    public void setReferenceTerminals(Set<Terminal> terminals) {
        Objects.requireNonNull(terminals);
        terminals.forEach(t -> checkTerminalInNetwork(t, getExtendable()));
        terminalsPerVariant.set(getVariantIndex(), new LinkedHashSet<>(terminals));
    }

    @Override
    public ReferenceTerminals reset() {
        terminalsPerVariant.set(getVariantIndex(), new LinkedHashSet<>());
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
            terminalsPerVariant.add(new LinkedHashSet<>(sourceTerminals));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int i = 0; i < number; i++) {
            terminalsPerVariant.remove(terminalsPerVariant.size() - 1); // remove elements from the top to avoid moves inside the array
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        terminalsPerVariant.set(index, null);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        Set<Terminal> sourceTerminals = terminalsPerVariant.get(sourceIndex);
        for (int index : indexes) {
            terminalsPerVariant.set(index, new LinkedHashSet<>(sourceTerminals));
        }
    }

    private static void checkTerminalInNetwork(Terminal terminal, Network network) {
        if (!terminal.getVoltageLevel().getNetwork().equals(network)) {
            throw new PowsyblException("Terminal given is not in the right Network ("
                    + terminal.getVoltageLevel().getNetwork().getId() + " instead of " + network.getId() + ")");
        }
    }
}
