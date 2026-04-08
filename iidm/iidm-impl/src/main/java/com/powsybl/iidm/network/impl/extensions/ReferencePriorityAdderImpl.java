/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.ReferencePriorityAdder;

import java.util.Objects;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class ReferencePriorityAdderImpl implements ReferencePriorityAdder {

    ReferencePrioritiesImpl<? extends Connectable<?>> referencePriorities;

    private Terminal terminal;
    private int priority = -1;

    public ReferencePriorityAdderImpl(ReferencePrioritiesImpl<? extends Connectable<?>> referencePriorities) {
        this.referencePriorities = Objects.requireNonNull(referencePriorities);
    }

    @Override
    public ReferencePriorityAdder setTerminal(Terminal terminal) {
        this.terminal = terminal;
        return this;
    }

    @Override
    public ReferencePriorityAdder setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public ReferencePriority add() {
        ReferencePriority referencePriority = new ReferencePriorityImpl(terminal, priority);
        referencePriorities.add(referencePriority);
        return referencePriority;
    }
}
