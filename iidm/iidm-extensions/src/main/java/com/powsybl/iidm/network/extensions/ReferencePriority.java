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
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface ReferencePriority {

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

    Terminal getTerminal();

    int getPriority();
}
