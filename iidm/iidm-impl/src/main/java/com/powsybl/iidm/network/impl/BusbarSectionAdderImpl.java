/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.BusbarSectionAdder;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusbarSectionAdderImpl extends AbstractIdentifiableAdder<BusbarSectionAdderImpl> implements BusbarSectionAdder {

    private final VoltageLevelExt voltageLevel;

    private Integer node;

    BusbarSectionAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "Busbar section";
    }

    @Override
    public BusbarSectionAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public BusbarSection add() {
        String id = checkAndGetUniqueId();
        TerminalExt terminal = new NodeTerminal(getNetwork().getRef(), node);
        BusbarSectionImpl section = new BusbarSectionImpl(id, getName());
        section.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getObjectStore().checkAndAdd(section);
        getNetwork().getListeners().notifyCreation(section);
        return section;
    }

}
