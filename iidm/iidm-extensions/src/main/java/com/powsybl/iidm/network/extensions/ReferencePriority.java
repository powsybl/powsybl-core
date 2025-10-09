/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Optional;

/**
 * ReferencePriority iIDM extension allow to specify priority for a Terminal.
 * A priority 0 means should not be used.
 * 1 is highest priority for selection.
 * 2 is second highest priority, etc ...
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 * @see ReferencePriorities
 */
public interface ReferencePriority {

    /**
     * get the terminal having a reference priority defined
     * @return the terminal
     */
    Terminal getTerminal();

    /**
     * get the terminal reference priority
     * @return reference priority value
     */
    int getPriority();

    /**
     * Get the reference priority of an injection.
     * @param injection an injection
     * @return reference priority value
     */
    static int get(Injection<?> injection) {
        ReferencePriorities ext = injection.getExtension(ReferencePriorities.class);
        if (ext == null || ext.getReferencePriorities().isEmpty()) {
            return 0;
        }
        return ((ReferencePriority) (ext.getReferencePriorities().get(0))).getPriority();
    }

    /**
     * Get the priority of a branch side.
     * @param branch a branch
     * @param side side to get priority from
     * @return reference priority value
     */
    static int get(Branch<?> branch, TwoSides side) {
        ReferencePriorities ext = branch.getExtension(ReferencePriorities.class);
        if (ext == null) {
            return 0;
        }
        Optional<ReferencePriority> refTerminal = ((List<ReferencePriority>) (ext.getReferencePriorities())).stream()
                .filter(rt -> rt.getTerminal().getConnectable().getId().equals(branch.getId()))
                .filter(rt -> side.toThreeSides().equals(Terminal.getConnectableSide(rt.getTerminal()).orElseThrow()))
                .findFirst();
        return refTerminal.map(ReferencePriority::getPriority).orElse(0);
    }

    /**
     * Get the priority of a three windings transformer side.
     * @param threeWindingsTransformer a three windings transformer
     * @param side side to get priority from
     * @return reference priority value
     */
    static int get(ThreeWindingsTransformer threeWindingsTransformer, ThreeSides side) {
        ReferencePriorities ext = threeWindingsTransformer.getExtension(ReferencePriorities.class);
        if (ext == null) {
            return 0;
        }
        Optional<ReferencePriority> refTerminal = ((List<ReferencePriority>) (ext.getReferencePriorities())).stream()
                .filter(rt -> rt.getTerminal().getConnectable().getId().equals(threeWindingsTransformer.getId()))
                .filter(rt -> side.equals(Terminal.getConnectableSide(rt.getTerminal()).orElseThrow()))
                .findFirst();
        return refTerminal.map(ReferencePriority::getPriority).orElse(0);
    }

    /**
     * Set the reference priority of an injection.
     * @param injection an injection
     * @param priority priority value to set
     */
    static void set(Injection<?> injection, int priority) {
        ReferencePriorities ext = injection.getExtension(ReferencePriorities.class);
        if (ext == null) {
            ext = (ReferencePriorities) injection.newExtension(ReferencePrioritiesAdder.class).add();
        }
        ext.newReferencePriority()
            .setTerminal(injection.getTerminals().get(0))
            .setPriority(priority)
            .add();
    }

    /**
     * Set the reference priority of a branch side.
     * @param branch a branch
     * @param side side to set priority to
     * @param priority priority value to set
     */
    static void set(Branch<?> branch, TwoSides side, int priority) {
        ReferencePriorities ext = branch.getExtension(ReferencePriorities.class);
        if (ext == null) {
            ext = (ReferencePriorities) branch.newExtension(ReferencePrioritiesAdder.class).add();
        }
        ext.newReferencePriority()
            .setTerminal(branch.getTerminal(side))
            .setPriority(priority)
            .add();
    }

    /**
     * Set the reference priority of a three windings transformer side.
     * @param threeWindingsTransformer a three windings transformer
     * @param side side to set priority to
     * @param priority priority value to set
     */
    static void set(ThreeWindingsTransformer threeWindingsTransformer, ThreeSides side, int priority) {
        ReferencePriorities ext = threeWindingsTransformer.getExtension(ReferencePriorities.class);
        if (ext == null) {
            ext = (ReferencePriorities) threeWindingsTransformer.newExtension(ReferencePrioritiesAdder.class).add();
        }
        ext.newReferencePriority()
            .setTerminal(threeWindingsTransformer.getTerminal(side))
            .setPriority(priority)
            .add();
    }
}
