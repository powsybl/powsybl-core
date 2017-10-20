/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Connectable;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractConnectable<I extends Connectable<I>> extends AbstractIdentifiable<I> implements Connectable<I>, Stateful {

    protected final List<TerminalExt> terminals = new ArrayList<>();

    AbstractConnectable(String id, String name) {
        super(id, name);
    }

    void addTerminal(TerminalExt terminal) {
        terminals.add(terminal);
        terminal.setConnectable(this);
    }

    public List<TerminalExt> getTerminals() {
        return terminals;
    }

    protected NetworkImpl getNetwork() {
        if (terminals.isEmpty()) {
            throw new PowsyblException(id + " is not attached to a network");
        }
        return terminals.get(0).getVoltageLevel().getNetwork();
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();
        network.getObjectStore().remove(this);
        for (TerminalExt terminal : terminals) {
            VoltageLevelExt vl = terminal.getVoltageLevel();
            vl.detach(terminal);
            vl.clean();
        }
        network.getListeners().notifyRemoval(this);
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        for (TerminalExt t : terminals) {
            t.extendStateArraySize(initStateArraySize, number, sourceIndex);
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        for (TerminalExt t : terminals) {
            t.reduceStateArraySize(number);
        }
    }

    @Override
    public void deleteStateArrayElement(int index) {
        for (TerminalExt t : terminals) {
            t.deleteStateArrayElement(index);
        }
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        for (TerminalExt t : terminals) {
            t.allocateStateArrayElement(indexes, sourceIndex);
        }
    }

}
