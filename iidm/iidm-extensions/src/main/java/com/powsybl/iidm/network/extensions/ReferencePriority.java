/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

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
     * Get the reference priority of a connectable with only one terminal.
     * @param connectable a connectable
     * @return reference priority value
     * @throws PowsyblException if the connectable has multiple terminals
     */
    static int get(Connectable<?> connectable) {
        if (connectable.getTerminals().size() != 1) {
            throw new PowsyblException("This method can only be used on a connectable having a single Terminal");
        }
        ReferencePriorities ext = connectable.getExtension(ReferencePriorities.class);
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
    static int get(Branch<?> branch, Branch.Side side) {
        ReferencePriorities ext = branch.getExtension(ReferencePriorities.class);
        if (ext == null) {
            return 0;
        }
        Optional<ReferencePriority> refTerminal = ((List<ReferencePriority>) (ext.getReferencePriorities())).stream()
                .filter(rt -> rt.getTerminal().equals(branch.getTerminal(side)))
                .findFirst();
        return refTerminal.map(ReferencePriority::getPriority).orElse(0);
    }

    /**
     * Get the priority of a three windings transformer side.
     * @param threeWindingsTransformer a three windings transformer
     * @param side side to get priority from
     * @return reference priority value
     */
    static int get(ThreeWindingsTransformer threeWindingsTransformer, ThreeWindingsTransformer.Side side) {
        ReferencePriorities ext = threeWindingsTransformer.getExtension(ReferencePriorities.class);
        if (ext == null) {
            return 0;
        }
        Optional<ReferencePriority> refTerminal = ((List<ReferencePriority>) (ext.getReferencePriorities())).stream()
                .filter(rt -> rt.getTerminal().equals(threeWindingsTransformer.getTerminal(side)))
                .findFirst();
        return refTerminal.map(ReferencePriority::getPriority).orElse(0);
    }

    /**
     * Set the reference priority of a connectable with only one terminal.
     * @param connectable a connectable
     * @param priority priority value to set
     * @throws PowsyblException if the connectable has multiple terminals
     */
    static void set(Connectable<?> connectable, int priority) {
        if (connectable.getTerminals().size() != 1) {
            throw new PowsyblException("This method can only be used on a connectable having a single Terminal");
        }
        connectable.newExtension(ReferencePrioritiesAdder.class).add();
        connectable.getExtension(ReferencePriorities.class)
                .newReferencePriority()
                .setTerminal(connectable.getTerminals().get(0))
                .setPriority(priority)
                .add();
    }

    /**
     * Set the reference priority of a branch side.
     * @param branch a branch
     * @param side side to set priority to
     * @param priority priority value to set
     */
    static void set(Branch<?> branch, Branch.Side side, int priority) {
        if (branch.getExtension(ReferencePriorities.class) == null) {
            branch.newExtension(ReferencePrioritiesAdder.class).add();
        }
        branch.getExtension(ReferencePriorities.class)
                .newReferencePriority()
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
    static void set(ThreeWindingsTransformer threeWindingsTransformer, ThreeWindingsTransformer.Side side, int priority) {
        if (threeWindingsTransformer.getExtension(ReferencePriorities.class) == null) {
            threeWindingsTransformer.newExtension(ReferencePrioritiesAdder.class).add();
        }
        threeWindingsTransformer.getExtension(ReferencePriorities.class)
                .newReferencePriority()
                .setTerminal(threeWindingsTransformer.getTerminal(side))
                .setPriority(priority)
                .add();
    }
}
