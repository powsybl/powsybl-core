/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface Boundary {

    /**
     * Get the voltage of the boundary fictitious bus.
     */
    double getV();

    /**
     * Get the angle of the boundary fictitious bus.
     */
    double getAngle();

    /**
     * Get the active power at the fictitious terminal going from the boundary fictitious bus to the network.
     */
    double getP();

    /**
     * Get the reactive power at the fictitious terminal going from the boundary fictitious bus to the network.
     */
    double getQ();

    /**
     * A Boundary could be associated with one side of a branch to determine P and Q.
     * Get the branch side the boundary refers to.
     */
    default Branch.Side getSide() {
        return null;
    }

    /**
     * Get the equipment the boundary is associated to.
     */
    default Connectable getConnectable() {
        return null;
    }

    /**
     * Get the voltage level at network side.
     */
    default VoltageLevel getNetworkSideVoltageLevel() {
        Connectable<?> c = getConnectable();
        if (c == null) {
            throw new IllegalStateException("Missing parent connectable");
        } else if (c instanceof Injection) {
            return ((Injection) c).getTerminal().getVoltageLevel();
        } else if (c instanceof Branch) {
            return ((Branch) c).getTerminal(getSide()).getVoltageLevel();
        }
        throw new IllegalStateException("Unexpected parent connectable: " + c.getId());
    }
}
