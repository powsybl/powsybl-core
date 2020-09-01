/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.ConnectableType;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusbarSectionImpl extends AbstractConnectable<BusbarSection> implements BusbarSection {

    // After a state estimation calculation,
    // fictitious injections may be attributed to the busbar sections
    // in node/breaker topology.
    double fictitiousP;
    double fictitiousQ;

    BusbarSectionImpl(String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        this.fictitiousP = Double.NaN;
        this.fictitiousQ = Double.NaN;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.BUSBAR_SECTION;
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    protected String getTypeDescription() {
        return "Busbar section";
    }

    @Override
    public double getV() {
        return ((NodeTerminal) getTerminal()).getV();
    }

    @Override
    public double getAngle() {
        return ((NodeTerminal) getTerminal()).getAngle();
    }

    @Override
    public double getFictitiousP() {
        return fictitiousP;
    }

    @Override
    public double getFictitiousQ() {
        return fictitiousQ;
    }

    @Override
    public BusbarSection setFictitiousP(double p) {
        fictitiousP = p;
        return this;
    }

    @Override
    public BusbarSection setFictitiousQ(double q) {
        fictitiousQ = q;
        return this;
    }
}
