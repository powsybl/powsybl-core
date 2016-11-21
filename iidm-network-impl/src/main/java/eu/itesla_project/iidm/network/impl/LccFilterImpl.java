/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.LccFilter;

import java.util.BitSet;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class LccFilterImpl implements LccFilter, Stateful, Validable {

    private float b;

    private final BitSet connected;

    private final LccConverterStationImpl converterStation;

    LccFilterImpl(LccConverterStationImpl converterStation, float b, boolean connected) {
        this.converterStation = Objects.requireNonNull(converterStation);
        this.b = b;

        int stateArraySize = getNetwork().getStateManager().getStateArraySize();
        this.connected = new BitSet(stateArraySize);
        this.connected.set(0, stateArraySize, connected);
    }

    @Override
    public String getMessageHeader() {
        return converterStation.getMessageHeader() + "filter ";
    }

    private NetworkImpl getNetwork() {
        return converterStation.getNetwork();
    }

    private void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        converterStation.notifyFilterUpdate(this, attribute, oldValue, newValue);
    }

    @Override
    public float getB() {
        return b;
    }

    @Override
    public LccFilterImpl setB(float b) {
        ValidationUtil.checkB(this, b);
        float oldValue = this.b;
        this.b = b;
        notifyUpdate("b", oldValue, b);
        return this;
    }

    @Override
    public boolean isConnected() {
        return connected.get(getNetwork().getStateIndex());
    }

    @Override
    public LccFilterImpl setConnected(boolean connected) {
        int stateIndex = getNetwork().getStateIndex();
        boolean oldValue = this.connected.get(stateIndex);
        this.connected.set(stateIndex, connected);
        notifyUpdate("connected", oldValue, connected);
        return this;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        connected.set(initStateArraySize, initStateArraySize + number, connected.get(sourceIndex));
    }

    @Override
    public void reduceStateArraySize(int number) {
        // Nothing to do
    }

    @Override
    public void deleteStateArrayElement(int index) {
        // Nothing to do
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            connected.set(index, connected.get(sourceIndex));
        }
    }
}
