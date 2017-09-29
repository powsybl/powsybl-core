/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A switch to connect equipments in a substation.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Switch extends Identifiable<Switch> {

    /**
     * Get the parent voltage level.
     * @return the parent voltage level
     */
    VoltageLevel getVoltageLevel();

    /**
     * Get the kind of switch.
     */
    SwitchKind getKind();

    /**
     * Get the open status of the switch.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    boolean isOpen();

    /**
     * Change the switch status.
     * <p>
     * Depends on the working state.
     * @param open the new switch status
     * @see StateManager
     */
    void setOpen(boolean open);

    /**
     * Get the retain status of the switch. A retained switch is a switch that
     * will be part of the bus/breaker topology.
     */
    boolean isRetained();

    /**
     * Change the retain status of the switch. A retained switch is a switch that
     * will be part of the bus/breaker topology.
     * @param retained the retain status of the switch
     */
    void setRetained(boolean retained);

    /**
     * Get the fictitious status of the switch
     */
    boolean isFictitious();

    /**
     * Set the fictitious status of the switch
     */
    void setFictitious(boolean fictitious);
}
