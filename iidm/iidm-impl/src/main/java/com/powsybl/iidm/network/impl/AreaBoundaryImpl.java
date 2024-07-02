/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaBoundary;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;
import java.util.Optional;

public class AreaBoundaryImpl implements AreaBoundary {

    final Area area;

    final Terminal terminal;

    final Boundary boundary;

    final boolean ac;

    AreaBoundaryImpl(Area area, Terminal terminal, boolean ac) {
        this.area = Objects.requireNonNull(area);
        this.terminal = Objects.requireNonNull(terminal);
        this.boundary = null;
        this.ac = ac;
    }

    AreaBoundaryImpl(Area area, Boundary boundary, boolean ac) {
        this.area = Objects.requireNonNull(area);
        this.boundary = Objects.requireNonNull(boundary);
        this.terminal = null;
        this.ac = ac;
    }

    @Override
    public Area getArea() {
        return area;
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
