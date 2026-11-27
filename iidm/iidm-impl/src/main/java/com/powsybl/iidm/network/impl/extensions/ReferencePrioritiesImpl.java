/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.ReferencePriorityAdder;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import com.powsybl.iidm.network.impl.NetworkImpl;

import java.util.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ReferencePrioritiesImpl<C extends Connectable<C>> extends AbstractMultiVariantIdentifiableExtension<C> implements ReferencePriorities<C> {

    private final ArrayList<Map<Terminal, ReferencePriority>> referencePrioritiesPerVariant;

    ReferencePrioritiesImpl(C extendable) {
        super(extendable);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.referencePrioritiesPerVariant = new ArrayList<>(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.referencePrioritiesPerVariant.add(new LinkedHashMap<>());
        }
    }

    ReferencePrioritiesImpl<C> add(ReferencePriority referencePriority) {
        if (!getExtendable().getTerminals().contains(referencePriority.getTerminal())) {
            throw new PowsyblException(String.format("The provided terminal does not belong to the connectable %s",
                getExtendable().getId()));
        }

        int variantIndex = getVariantIndex();
        ReferencePriority oldReferencePriority = referencePrioritiesPerVariant.get(variantIndex).put(referencePriority.getTerminal(), referencePriority);
        int oldPriority = oldReferencePriority != null ? oldReferencePriority.getPriority() : 0;
        int newPriority = referencePriority.getPriority();

        if (oldPriority != newPriority) {
            NetworkImpl network = (NetworkImpl) getExtendable().getNetwork();
            String variantId = getVariantManagerHolder().getVariantManager().getWorkingVariantId();
            network.getListeners().notifyExtensionUpdate(this, getNotifyAttribute(referencePriority), variantId, oldPriority, newPriority);
        }

        return this;
    }

    private String getNotifyAttribute(ReferencePriority referencePriority) {
        return getExtendable().getTerminals().size() > 1
                ? "referencePriority" + referencePriority.getTerminal().getSide().getNum()
                : "referencePriority";
    }

    @Override
    public ReferencePriorityAdder newReferencePriority() {
        return new ReferencePriorityAdderImpl(this);
    }

    @Override
    public List<ReferencePriority> getReferencePriorities() {
        return referencePrioritiesPerVariant.get(getVariantIndex()).values().stream().toList();
    }

    @Override
    public void deleteReferencePriorities() {
        int variantIndex = getVariantIndex();
        Map<Terminal, ReferencePriority> referencePriorities = referencePrioritiesPerVariant.get(variantIndex);

        List<ReferencePriority> oldReferencePriorities = new ArrayList<>(referencePriorities.values());
        referencePriorities.clear();

        NetworkImpl network = (NetworkImpl) getExtendable().getNetwork();
        String variantId = getVariantManagerHolder().getVariantManager().getWorkingVariantId();
        for (ReferencePriority oldReferencePriority : oldReferencePriorities) {
            if (oldReferencePriority.getPriority() != 0) {
                network.getListeners().notifyExtensionUpdate(this, getNotifyAttribute(oldReferencePriority), variantId, oldReferencePriority.getPriority(), 0);
            }
        }
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        referencePrioritiesPerVariant.ensureCapacity(referencePrioritiesPerVariant.size() + number);
        Map<Terminal, ReferencePriority> sourcePriorities = referencePrioritiesPerVariant.get(sourceIndex);
        for (int i = 0; i < number; ++i) {
            referencePrioritiesPerVariant.add(new LinkedHashMap<>(sourcePriorities));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        for (int i = 0; i < number; i++) {
            referencePrioritiesPerVariant.remove(referencePrioritiesPerVariant.size() - 1); // remove elements from the top to avoid moves inside the array
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        referencePrioritiesPerVariant.set(index, null);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        Map<Terminal, ReferencePriority> sourcePriorities = referencePrioritiesPerVariant.get(sourceIndex);
        for (int index : indexes) {
            referencePrioritiesPerVariant.set(index, new LinkedHashMap<>(sourcePriorities));
        }
    }
}
