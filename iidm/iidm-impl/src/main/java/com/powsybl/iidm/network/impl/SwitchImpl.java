/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;

import java.util.BitSet;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SwitchImpl extends AbstractIdentifiable<Switch> implements Switch, Stateful {

    private final VoltageLevelExt voltageLevel;

    private final SwitchKind kind;

    private boolean fictitious;

    private final BitSet open;

    private final BitSet retained;

    SwitchImpl(VoltageLevelExt voltageLevel,
               String id, String name, SwitchKind kind, final boolean open, boolean retained, boolean fictitious) {
        super(id, name);
        this.voltageLevel = voltageLevel;
        this.kind = kind;
        this.fictitious = fictitious;
        int stateArraySize = voltageLevel.getNetwork().getStateManager().getStateArraySize();
        this.open = new BitSet(stateArraySize);
        this.open.set(0, stateArraySize, open);
        this.retained = new BitSet(stateArraySize);
        this.retained.set(0, stateArraySize, retained);
    }

    @Override
    public VoltageLevelExt getVoltageLevel() {
        return voltageLevel;
    }

    @Override
    public SwitchKind getKind() {
        return kind;
    }

    @Override
    public boolean isOpen() {
        return open.get(voltageLevel.getNetwork().getStateIndex());
    }

    @Override
    public void setOpen(boolean open) {
        NetworkImpl network = voltageLevel.getNetwork();
        int index = network.getStateIndex();
        boolean oldValue = this.open.get(index);
        if (oldValue != open) {
            this.open.set(index, open);
            voltageLevel.invalidateCache();
            network.getListeners().notifyUpdate(this, "open", oldValue, open);
        }
    }

    @Override
    public boolean isRetained() {
        return retained.get(voltageLevel.getNetwork().getStateIndex());
    }

    @Override
    public void setRetained(boolean retained) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new ValidationException(this, "retain status is not modifiable in a non node/breaker voltage level");
        }
        NetworkImpl network = voltageLevel.getNetwork();
        int index = network.getStateIndex();
        boolean oldValue = this.retained.get(index);
        if (oldValue != retained) {
            this.retained.set(index, retained);
            voltageLevel.invalidateCache();
            network.getListeners().notifyUpdate(this, "retained", oldValue, retained);
        }
    }

    @Override
    public boolean isFictitious() {
        return fictitious;
    }

    @Override
    public void setFictitious(boolean fictitious) {
        boolean oldValue = this.fictitious;
        if (oldValue != fictitious) {
            this.fictitious = fictitious;
            voltageLevel.invalidateCache();
            NetworkImpl network = voltageLevel.getNetwork();
            network.getListeners().notifyUpdate(this, "fictitious", oldValue, fictitious);
        }
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        this.open.set(initStateArraySize, initStateArraySize + number, this.open.get(sourceIndex));
        this.retained.set(initStateArraySize, initStateArraySize + number, this.retained.get(sourceIndex));
    }

    @Override
    public void reduceStateArraySize(int number) {
        // nothing to do
    }

    @Override
    public void deleteStateArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, final int sourceIndex) {
        for (int index : indexes) {
            open.set(index, open.get(sourceIndex));
            retained.set(index, retained.get(sourceIndex));
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Switch";
    }

}
