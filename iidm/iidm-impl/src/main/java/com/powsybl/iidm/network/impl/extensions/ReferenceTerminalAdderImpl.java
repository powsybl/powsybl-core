package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminalAdder;

import java.util.Objects;

public class ReferenceTerminalAdderImpl implements ReferenceTerminalAdder {

    ReferenceTerminalsImpl<? extends Connectable<?>> referenceTerminals;

    private Terminal terminal;
    private int priority = -1;

    public ReferenceTerminalAdderImpl(ReferenceTerminalsImpl<? extends Connectable<?>> referenceTerminals) {
        this.referenceTerminals = Objects.requireNonNull(referenceTerminals);
    }

    @Override
    public ReferenceTerminalAdder setTerminal(Terminal terminal) {
        this.terminal = terminal;
        return this;
    }

    @Override
    public ReferenceTerminalAdder setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public ReferenceTerminal add() {
        ReferenceTerminal referenceTerminal = new ReferenceTerminalImpl(terminal, priority);
        referenceTerminals.add(referenceTerminal);
        return referenceTerminal;
    }
}
