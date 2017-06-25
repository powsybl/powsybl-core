/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.Switch;
import eu.itesla_project.iidm.network.SwitchKind;
import eu.itesla_project.iidm.network.TopologyKind;

import java.util.BitSet;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SwitchImpl extends IdentifiableImpl<Switch> implements Switch, Stateful {

    private final VoltageLevelExt voltageLevel;

    private final SwitchKind kind;

    private boolean retained;

    private boolean ficticious;

    private final BitSet open;

    SwitchImpl(VoltageLevelExt voltageLevel,
               String id, String name, SwitchKind kind, final boolean open, boolean retained, boolean ficticious) {
        super(id, name);
        this.voltageLevel = voltageLevel;
        this.kind = kind;
        this.retained = retained;
        this.ficticious = ficticious;
        int stateArraySize = voltageLevel.getNetwork().getStateManager().getStateArraySize();
        this.open = new BitSet(stateArraySize);
        this.open.set(0, stateArraySize, open);
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
        return retained;
    }

    @Override
    public void setRetained(boolean retained) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new ValidationException(this, "retain status is not modifiable in a non node/breaker voltage level");
        }
        boolean oldValue = this.retained;
        if (oldValue != retained) {
            this.retained = retained;
            voltageLevel.invalidateCache();
            NetworkImpl network = voltageLevel.getNetwork();
            network.getListeners().notifyUpdate(this, "retained", oldValue, retained);
        }
    }

    @Override
    public boolean isFicticious() {
        return ficticious;
    }

    @Override
    public void setFicticious(boolean ficticious) {
        boolean oldValue = this.ficticious;
        if (oldValue != ficticious) {
            this.ficticious = ficticious;
            voltageLevel.invalidateCache();
            NetworkImpl network = voltageLevel.getNetwork();
            network.getListeners().notifyUpdate(this, "ficticious", oldValue, ficticious);
        }
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        for (int i = 0; i < number; i++) {
            this.open.set(initStateArraySize + i, this.open.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
    }

    @Override
    public void deleteStateArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, final int sourceIndex) {
        for (int index : indexes) {
            open.set(index, open.get(sourceIndex));
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Switch";
    }

}
