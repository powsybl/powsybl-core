package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AreaBoundary;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;
import java.util.Optional;

public class AreaBoundaryImpl implements AreaBoundary {
    final Terminal terminal;

    final Boundary boundary;

    final boolean ac;

    AreaBoundaryImpl(Terminal terminal, boolean ac) {
        this.terminal = Objects.requireNonNull(terminal);
        this.boundary = null;
        this.ac = ac;
    }

    AreaBoundaryImpl(Boundary boundary, boolean ac) {
        this.boundary = Objects.requireNonNull(boundary);
        this.terminal = null;
        this.ac = ac;
    }

    @Override
    public Optional<Terminal> getTerminal() {
        return Optional.ofNullable(terminal);
    }

    @Override
    public Optional<Boundary> getBoundary() {
        return Optional.ofNullable(boundary);
    }

    @Override
    public boolean isAc() {
        return ac;
    }

    @Override
    public double getP() {
        return boundary != null ? boundary.getP() : terminal.getP();
    }

    @Override
    public double getQ() {
        return boundary != null ? boundary.getQ() : terminal.getQ();
    }
}
