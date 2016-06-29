/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.Switch;
import eu.itesla_project.iidm.network.SwitchKind;
import eu.itesla_project.iidm.network.impl.util.Ref;
import java.util.BitSet;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SwitchImpl extends IdentifiableImpl<Switch> implements Switch, Stateful {

    private final Ref<NetworkImpl> network;

    private final SwitchKind kind;

    private boolean retained;

    private final BitSet open;

    SwitchImpl(Ref<NetworkImpl> network,
               String id, String name, SwitchKind kind, final boolean open, boolean retained) {
        super(id, name);
        this.network = network;
        this.kind = kind;
        this.retained = retained;
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        this.open = new BitSet(stateArraySize);
        this.open.set(0, stateArraySize, open);
    }

    @Override
    public SwitchKind getKind() {
        return kind;
    }

    @Override
    public boolean isOpen() {
        return open.get(network.get().getStateIndex());
    }

    void setOpen(boolean open) {
        int index = network.get().getStateIndex();
        boolean oldValue = this.open.get(index);
        this.open.set(index, open);
        network.get().getListeners().notifyUpdate(this, "open", oldValue, open);
    }

    @Override
    public boolean isRetained() {
        return retained;
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
