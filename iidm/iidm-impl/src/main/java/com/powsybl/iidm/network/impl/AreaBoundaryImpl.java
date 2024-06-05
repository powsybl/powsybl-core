package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AreaBoundary;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;
import java.util.Optional;

public class AreaBoundaryImpl implements AreaBoundary {
    final Terminal terminal;

    final DanglingLine danglingLine;

    final boolean ac;

    AreaBoundaryImpl(Terminal terminal, boolean ac) {
        this.terminal = Objects.requireNonNull(terminal);
        this.danglingLine = null;
        this.ac = Objects.requireNonNull(ac);
    }

    AreaBoundaryImpl(DanglingLine danglingLine, boolean ac) {
        this.danglingLine = Objects.requireNonNull(danglingLine);
        this.terminal = null;
        this.ac = Objects.requireNonNull(ac);
    }

    @Override
    public Optional<Terminal> getTerminal() {
        return Optional.ofNullable(terminal);
    }

    @Override
    public Optional<DanglingLine> getDanglingLine() {
        return Optional.ofNullable(danglingLine);
    }

    @Override
    public boolean isAc() {
        return ac;
    }

    @Override
    public double getP() {
        return danglingLine != null ? danglingLine.getP0() : terminal.getP();
    }

    @Override
    public double getQ() {
        return danglingLine != null ? danglingLine.getQ0() : terminal.getQ();
    }
}
