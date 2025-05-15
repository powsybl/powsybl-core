/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * A switch to connect equipments in a substation.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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
     * Depends on the working variant.
     * @see VariantManager
     */
    boolean isOpen();

    /**
     * Change the switch status.
     * <p>
     * Depends on the working variant.
     * @param open the new switch status
     * @see VariantManager
     */
    void setOpen(boolean open);

    /**
     * Get the solved open status of the switch.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Boolean isSolvedOpen();

    /**
     * Get the solved open status of the switch if it is defined. Otherwise, get an empty Optional.
     *
     * <p>
     *     Depends on the working variant.
     * </p>
     */
    default Optional<Boolean> findSolvedOpen() {
        return isSolvedOpen() == null ? Optional.empty() : Optional.of(isSolvedOpen());
    }

    /**
     * Change the switch solved status.
     * <p>
     * Depends on the working variant.
     * @param solvedOpen the new switch status
     * @see VariantManager
     */
    void setSolvedOpen(Boolean solvedOpen);

    /**
     * Unset the switch solved status.
     */
    void unsetSolvedOpen();

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

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.SWITCH;
    }

    default void setOpenToSolvedOpen() {
        this.findSolvedOpen().ifPresent(this::setOpen);
    }
}
