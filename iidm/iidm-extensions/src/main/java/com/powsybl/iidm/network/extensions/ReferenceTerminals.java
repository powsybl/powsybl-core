/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;

import java.util.Comparator;
import java.util.List;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface ReferenceTerminals<C extends Connectable<C>> extends Extension<C> {

    String NAME = "referenceTerminals";

    Comparator<ReferenceTerminal> PRIORITY_COMPARATOR = Comparator.comparing(ReferenceTerminal::getPriority, (p1, p2) -> {
        if ((p1 == 0 || p2 == 0) && !p1.equals(p2)) {
            return p1 == 0 ? 1 : -1;
        }
        return p1 - p2;
    });

    Comparator<ReferenceTerminal> DEFAULT_CONNECTABLE_TYPE_COMPARATOR = Comparator.comparing(rt -> rt.getTerminal().getConnectable().getType(), (t1, t2) -> {
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

    static List<ReferenceTerminal> getReferenceTerminals(Network network, Comparator<ReferenceTerminal> connectableComparator) {
        return network.getConnectableStream().filter(c -> c.getExtension(ReferenceTerminals.class) != null)
                .map(c -> (ReferenceTerminals) (c.getExtension(ReferenceTerminals.class)))
                .flatMap(rt -> rt.getReferenceTerminals().stream())
                .sorted(PRIORITY_COMPARATOR.thenComparing(connectableComparator))
                .toList();
    }

    static List<ReferenceTerminal> getReferenceTerminals(Network network) {
        return getReferenceTerminals(network, DEFAULT_CONNECTABLE_TYPE_COMPARATOR);
    }

    static void deleteReferenceTerminals(Network network) {
        network.getConnectableStream().forEach(c -> c.removeExtension(ReferenceTerminals.class));
    }

    @Override
    default String getName() {
        return NAME;
    }

    ReferenceTerminalAdder newReferenceTerminal();

    List<ReferenceTerminal> getReferenceTerminals();

}
