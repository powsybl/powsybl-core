/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Terminal;

import java.util.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class CgmesControlAreaImpl implements CgmesControlArea {
    private final String id;
    private final String name;
    private final String energyIdentificationCodeEic;
    private final Set<Terminal> terminals = new LinkedHashSet<>();
    private final Set<Boundary> boundaries = new LinkedHashSet<>();
    private double netInterchange;
    private double pTolerance;

    CgmesControlAreaImpl(String id, String name, String energyIdentificationCodeEic, double netInterchange, double pTolerance, CgmesControlAreasImpl mapping) {
        this.id = Objects.requireNonNull(id);
        this.name = name;
        this.energyIdentificationCodeEic = energyIdentificationCodeEic;
        this.netInterchange = netInterchange;
        this.pTolerance = pTolerance;
        attach(mapping);
    }

    private void attach(CgmesControlAreasImpl mapping) {
        mapping.putCgmesControlArea(this);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEnergyIdentificationCodeEIC() {
        return energyIdentificationCodeEic;
    }

    @Override
    public Set<Terminal> getTerminals() {
        return Collections.unmodifiableSet(terminals);
    }

    @Override
    public Set<Boundary> getBoundaries() {
        return Collections.unmodifiableSet(boundaries);
    }

    @Override
    public double getNetInterchange() {
        return netInterchange;
    }

    @Override
    public void add(Terminal terminal) {
        terminals.add(terminal);
    }

    @Override
    public void add(Boundary boundary) {
        boundaries.add(boundary);
    }

    @Override
    public void setNetInterchange(double netInterchange) {
        this.netInterchange = netInterchange;
    }

    @Override
    public double getPTolerance() {
        return pTolerance;
    }

    @Override
    public void setPTolerance(double pTolerance) {
        this.pTolerance = pTolerance;
    }
}
