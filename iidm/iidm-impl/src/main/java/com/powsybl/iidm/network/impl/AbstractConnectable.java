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
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractConnectable<I extends Connectable<I>> extends AbstractIdentifiable<I> implements Connectable<I>, MultiVariantObject {

    protected final List<TerminalExt> terminals = new ArrayList<>();

    AbstractConnectable(String id, String name, boolean fictitious) {
        super(id, name, fictitious);
    }

    public I setName(String name) {
        String oldValue = this.name;
        this.name = name;
        notifyUpdate("name", oldValue, name);
        return (I) this;
    }

    void addTerminal(TerminalExt terminal) {
        terminals.add(terminal);
        terminal.setConnectable(this);
    }

    public List<TerminalExt> getTerminals() {
        return terminals;
    }

    @Override
    public NetworkImpl getNetwork() {
        if (terminals.isEmpty()) {
            throw new PowsyblException(id + " is not attached to a network");
        }

        // During the removal of a multi terminals component (Line, 2WT or 3WT), terminals are detached from the voltage level
        return terminals.stream()
                        .map(TerminalExt::getVoltageLevel)
                        .filter(Objects::nonNull)
                        .map(VoltageLevelExt::getNetwork)
                        .findFirst()
                        .orElse(null);
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();
        network.getIndex().remove(this);
        for (TerminalExt terminal : terminals) {
            VoltageLevelExt vl = terminal.getVoltageLevel();
            vl.detach(terminal);
        }
        network.getListeners().notifyRemoval(this);
    }

    protected void notifyUpdate(Supplier<String> attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, oldValue, newValue);
    }

    protected void notifyUpdate(Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, variantId, oldValue, newValue);
    }

    protected void notifyUpdate(String attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(this, attribute, variantId, oldValue, newValue);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);

        for (TerminalExt t : terminals) {
            t.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);

        for (TerminalExt t : terminals) {
            t.reduceVariantArraySize(number);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);

        for (TerminalExt t : terminals) {
            t.deleteVariantArrayElement(index);
        }
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);

        for (TerminalExt t : terminals) {
            t.allocateVariantArrayElement(indexes, sourceIndex);
        }
    }

}
