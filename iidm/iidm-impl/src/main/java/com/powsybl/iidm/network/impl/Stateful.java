/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

/**
 * An interface implemented by network objets that have attributes depending on
 * the state.
 * <p>
 * A class implementing this interface internally manages an array of state and
 * is notified when the array need to be resized thanks to <code>extendStateArraySize</code>
 * and <code>reduceStateArraySize</code> callbacks.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
interface Stateful {

    /**
     * Called to extend the state array.
     *
     * @param initStateArraySize initial state array size
     * @param number number of element to add
     * @param sourceIndex
     */
    void extendStateArraySize(int initStateArraySize, int number, int sourceIndex);

    /**
     * Called to reduce the state array.
     *
     * @param number number of element to remove
     */
    void reduceStateArraySize(int number);

    /**
     * Called to delete a state array element.
     *
     * @param index the index of the state array to delete
     */
    void deleteStateArrayElement(int index);

    /**
     * Called to allocate a state array element.
     *
     * @param indexes the indexes of the state array to allocate
     * @param sourceIndex
     */
    void allocateStateArrayElement(int[] indexes, int sourceIndex);

}
