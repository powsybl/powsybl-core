/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.ReferencePriorityAdder;
import com.powsybl.iidm.network.extensions.ReferencePriorities;

import java.util.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ReferencePrioritiesImpl<C extends Connectable<C>> extends AbstractExtension<C> implements ReferencePriorities<C> {

    private final Map<Terminal, ReferencePriority> referencePriorities = new LinkedHashMap<>();

    ReferencePrioritiesImpl<C> add(ReferencePriority referencePriority) {
        if (!getExtendable().getTerminals().contains(referencePriority.getTerminal())) {
            throw new PowsyblException("The provided terminal does not belong to this connectable");
        }
        referencePriorities.put(referencePriority.getTerminal(), referencePriority);
        return this;
    }

    @Override
    public ReferencePriorityAdder newReferencePriority() {
        return new ReferencePriorityAdderImpl(this);
    }

    @Override
    public List<ReferencePriority> getReferencePriorities() {
        return referencePriorities.values().stream().toList();
    }
}
