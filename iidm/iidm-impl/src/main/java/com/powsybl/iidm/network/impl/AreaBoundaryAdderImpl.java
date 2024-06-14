/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import java.util.Objects;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */

public class AreaBoundaryAdderImpl implements AreaBoundaryAdder {

    AreaImpl area;

    Boundary boundary;

    Terminal terminal;

    Boolean ac;

    AreaBoundaryAdderImpl(AreaImpl area) {
        this.area = Objects.requireNonNull(area);
    }

    protected AreaImpl getArea() {
        return area;
    }

    protected Boundary getBoundary() {
        return boundary;
    }

    protected Terminal getTerminal() {
        return terminal;
    }

    protected Boolean isAc() {
        return ac;
    }

    @Override
    public AreaBoundaryAdder setBoundary(Boundary boundary) {
        this.boundary = boundary;
        this.terminal = null;
        return this;
    }

    @Override
    public AreaBoundaryAdder setTerminal(Terminal terminal) {
        this.terminal = terminal;
        this.boundary = null;
        return this;
    }

    @Override
    public AreaBoundaryAdder setAc(boolean ac) {
        this.ac = ac;
        return this;
    }

    @Override
    public void add() {
        if (isAc() == null) {
            throw new PowsyblException("AreaBoundary AC flag is not set.");
        }
        // we remove before adding, to forbid duplicates and allow updating ac to true/false
        if (getBoundary() != null) {
            getArea().removeAreaBoundary(getBoundary());
            getArea().addAreaBoundary(new AreaBoundaryImpl(getBoundary(), isAc()));
        } else if (getTerminal() != null) {
            getArea().removeAreaBoundary(getTerminal());
            getArea().addAreaBoundary(new AreaBoundaryImpl(getTerminal(), isAc()));
        } else {
            throw new PowsyblException("No AreaBoundary element (terminal or boundary) is set.");
        }
    }

}
