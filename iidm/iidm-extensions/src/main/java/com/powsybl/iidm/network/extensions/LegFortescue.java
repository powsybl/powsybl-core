/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public interface LegFortescue {
    /**
     * Free fluxes set to true means that the magnetizing impedance Zm is infinite, i.e. fluxes are free.
     */
    boolean isFreeFluxes();

    void setFreeFluxes(boolean freeFluxes);

    /**
     * The zero-sequence resistance of the leg.
     */
    double getRz();

    void setRz(double rz);

    /**
     * The zero-sequence reactance of the leg.
     */
    double getXz();

    void setXz(double xz);

    /**
     * Get the winding connection type of the leg, see {@link WindingConnectionType}).
     */
    WindingConnectionType getConnectionType();

    void setConnectionType(WindingConnectionType connectionType);

    /**
     * If the leg is earthed, depending on {@link WindingConnectionType}, it represents the
     * resistance part of the impedance to ground.
     */
    double getGroundingR();

    void setGroundingR(double groundingR);

    /**
     * If the leg is earthed, depending on {@link WindingConnectionType}, it represents the
     * reactance part of the impedance to ground.
     */
    double getGroundingX();

    void setGroundingX(double groundingX);
}
