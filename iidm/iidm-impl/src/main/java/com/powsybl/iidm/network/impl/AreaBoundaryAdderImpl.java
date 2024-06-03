/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */

public class AreaBoundaryAdderImpl implements AreaBoundaryAdder {

    AreaImpl area;

    DanglingLine danglingLine;

    Terminal terminal;

    boolean ac;

    AreaBoundaryAdderImpl(AreaImpl area) {
        this.area = area;
    }

    protected AreaImpl getArea() {
        return area;
    }

    protected DanglingLine getDanglingLine() {
        return danglingLine;
    }

    protected Terminal getTerminal() {
        return terminal;
    }

    protected boolean isAc() {
        return ac;
    }

    @Override
    public AreaBoundaryAdder setDanglingLine(DanglingLine danglingLine) {
        this.danglingLine = danglingLine;
        this.terminal = null;
        return this;
    }

    @Override
    public AreaBoundaryAdder setTerminal(Terminal terminal) {
        this.terminal = terminal;
        this.danglingLine = null;
        return this;
    }

    @Override
    public AreaBoundaryAdder setAc(boolean ac) {
        this.ac = ac;
        return this;
    }

    @Override
    public void add() {
        if (danglingLine != null) {
            getArea().addAreaBoundary(new AreaBoundaryImpl(danglingLine, ac));
        } else {
            getArea().addAreaBoundary(new AreaBoundaryImpl(terminal, ac));
        }
    }

}
