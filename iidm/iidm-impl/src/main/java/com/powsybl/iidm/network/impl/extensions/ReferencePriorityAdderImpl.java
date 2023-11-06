package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.ReferencePriorityAdder;

import java.util.Objects;

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
