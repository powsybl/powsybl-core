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

    BusbarSectionImpl(String id, String name) {
        super(id, name);
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
}
