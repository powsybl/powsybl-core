/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A equipment with one terminal.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Injection<I extends Injection<I>> extends Connectable<I> {

    /**
     * Get the terminal.
     */
    Terminal getTerminal();

    /**
     * Get the active power in MW injected at this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default double getP() {
        return getTerminal().getP();
    }

    /**
     * Set the active power in MW injected at this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default I setP(double p) {
        getTerminal().setP(p);
        return (I) this;
    }

    /**
     * Get the reactive power in MVAR injected at this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default double getQ() {
        return getTerminal().getQ();
    }

    /**
     * Set the reactive power in MVAR injected at this equipment.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default I setQ(double q) {
        getTerminal().setQ(q);
        return (I) this;
    }

    /**
     * Get the current in A at this equipment.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    default double getI() {
        return getTerminal().getI();
    }

    /**
     * Try to connect this equipment.
     * <p>Depends on the working variant.
     * @return true if this equipment has been connected, false otherwise
     * @see VariantManager
     */
    default boolean connect() {
        return getTerminal().connect();
    }

    /**
     * Disconnect this equipment.
     * <p>Depends on the working variant.
     * @return true if this equipment has been disconnected, false otherwise
     * @see VariantManager
     */
    default boolean disconnect() {
        return getTerminal().disconnect();
    }

    /**
     * Test if this equipment is connected.
     * @return true if this equipment is connected, false otherwise
     */
    default boolean isConnected() {
        return getTerminal().isConnected();
    }

}
