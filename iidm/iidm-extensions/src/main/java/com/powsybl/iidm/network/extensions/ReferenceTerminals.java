/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface ReferenceTerminals<C extends Connectable<C>> extends Extension<C> {

    String NAME = "referenceTerminals";

    Comparator<ReferenceTerminal> DEFAULT_CONNECTABLE_TYPE_COMPARATOR = Comparator.comparing(rt -> rt.getTerminal().getConnectable().getType(), (t1, t2) -> {
        // favor generators over loads, loads over busbar sections, all others at the end
        if (t1 == t2) {
            return 0;
        } else if (t1 == IdentifiableType.GENERATOR || t2 == IdentifiableType.GENERATOR) {
            return t1 == IdentifiableType.GENERATOR ? -1 : 1;
        } else if (t1 == IdentifiableType.LOAD || t2 == IdentifiableType.LOAD) {
            return t1 == IdentifiableType.LOAD ? -1 : 1;
        } else if (t1 == IdentifiableType.BUSBAR_SECTION || t2 == IdentifiableType.BUSBAR_SECTION) {
            return t1 == IdentifiableType.BUSBAR_SECTION ? -1 : 1;
        }
        return 0;
    });

    static List<ReferenceTerminal> get(Network network, Comparator<ReferenceTerminal> connectableComparator) {
        return network.getConnectableStream().filter(c -> c.getExtension(ReferenceTerminals.class) != null)
                .map(c -> (ReferenceTerminals) (c.getExtension(ReferenceTerminals.class)))
                .flatMap(rt -> rt.getReferenceTerminals().stream())
                .filter(rt -> ((ReferenceTerminal) rt).getPriority() > 0)
                .sorted(Comparator.comparingInt(ReferenceTerminal::getPriority).thenComparing(connectableComparator))
                .toList();
    }

    static List<ReferenceTerminal> get(Network network) {
        return get(network, DEFAULT_CONNECTABLE_TYPE_COMPARATOR);
    }

    static void delete(Network network) {
        network.getConnectableStream().forEach(c -> c.removeExtension(ReferenceTerminals.class));
    }

    static int getPriority(Connectable<?> connectable) {
        if (connectable.getTerminals().size() != 1) {
            throw new PowsyblException("This method can only be used on a connectable having a single Terminal");
        }
        ReferenceTerminals ext = connectable.getExtension(ReferenceTerminals.class);
        if (ext == null || ext.getReferenceTerminals().isEmpty()) {
            return 0;
        }
        return ((ReferenceTerminal) (ext.getReferenceTerminals().get(0))).getPriority();
    }

    static int getPriority(Branch<?> branch, Branch.Side side) {
        ReferenceTerminals ext = branch.getExtension(ReferenceTerminals.class);
        if (ext == null || ext.getReferenceTerminals().isEmpty()) {
            return 0;
        }
        Optional<ReferenceTerminal> refTerminal = ((List<ReferenceTerminal>) (ext.getReferenceTerminals())).stream()
                .filter(rt -> rt.getTerminal().equals(branch.getTerminal(side)))
                .findFirst();
        return refTerminal.map(ReferenceTerminal::getPriority).orElse(0);
    }

    static int getPriority(ThreeWindingsTransformer threeWindingsTransformer, ThreeWindingsTransformer.Side side) {
        ReferenceTerminals ext = threeWindingsTransformer.getExtension(ReferenceTerminals.class);
        if (ext == null || ext.getReferenceTerminals().isEmpty()) {
            return 0;
        }
        Optional<ReferenceTerminal> refTerminal = ((List<ReferenceTerminal>) (ext.getReferenceTerminals())).stream()
                .filter(rt -> rt.getTerminal().equals(threeWindingsTransformer.getTerminal(side)))
                .findFirst();
        return refTerminal.map(ReferenceTerminal::getPriority).orElse(0);
    }

    static void setPriority(Connectable<?> connectable, int priority) {
        if (connectable.getTerminals().size() != 1) {
            throw new PowsyblException("This method can only be used on a connectable having a single Terminal");
        }
        connectable.newExtension(ReferenceTerminalsAdder.class).add();
        connectable.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setTerminal(connectable.getTerminals().get(0))
                .setPriority(priority)
                .add();
    }

    static void setPriority(Branch<?> branch, Branch.Side side, int priority) {
        if (branch.getExtension(ReferenceTerminals.class) == null) {
            branch.newExtension(ReferenceTerminalsAdder.class).add();
        }
        branch.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setTerminal(branch.getTerminal(side))
                .setPriority(priority)
                .add();
    }

    static void setPriority(ThreeWindingsTransformer threeWindingsTransformer, ThreeWindingsTransformer.Side side, int priority) {
        if (threeWindingsTransformer.getExtension(ReferenceTerminals.class) == null) {
            threeWindingsTransformer.newExtension(ReferenceTerminalsAdder.class).add();
        }
        threeWindingsTransformer.getExtension(ReferenceTerminals.class)
                .newReferenceTerminal()
                .setTerminal(threeWindingsTransformer.getTerminal(side))
                .setPriority(priority)
                .add();
    }

    @Override
    default String getName() {
        return NAME;
    }

    ReferenceTerminalAdder newReferenceTerminal();

    List<ReferenceTerminal> getReferenceTerminals();

}
