/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

/**
 * Interface to describe the characteristics of the fault to be simulated.
 * Used for elementary short-circuit calculation only.
 *
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
public interface Fault {

    // VERSION = 1.0
    String VERSION = "1.0";

    // How the fault impedance is connected to the network.
    enum ConnectionType {
        SERIES,
        PARALLEL,
    }

    // What kind of fault is simulated
    enum FaultType {
        THREE_PHASE,
        TWO_PHASE,
        SINGLE_PHASE,
    }

    //TODO : add the numbers of the phase for two and single phase

    // The equipment or bus id where the fault is simulated.
    String getId();

    default double getProportionalLocation() {
        return Double.NaN;
    }

    // Characteristics of the short-circuit.
    double getR();

    double getX();

    default ConnectionType getConnectionType() {
        return ConnectionType.SERIES;
    }

    default FaultType getFaultType() {
        return FaultType.THREE_PHASE;
    }

    // Whether the result should indicate a limit violation
    boolean withLimitViolations();

    // Whether the results should include the voltage map on the whole network
    boolean withVoltageMap();
}
