/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.List;
import java.util.stream.Stream;

/**
 * DC elements on which connectivity and topology can be examined: this is the case of DC Nodes and DC buses
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcTopologyVisitable {

    /**
     * Get the number of DC terminals.
     */
    int getDcTerminalCount();

    /**
     * Get the DC terminals.
     */
    List<DcTerminal> getDcTerminals();

    /**
     * Get the DC terminals.
     */
    Stream<DcTerminal> getDcTerminalStream();

    /**
     * Get the number of DC terminals connected.
     */
    int getConnectedDcTerminalCount();

    /**
     * Get the DC terminals connected.
     */
    List<DcTerminal> getConnectedDcTerminals();

    /**
     * Get the DC terminals connected.
     */
    Stream<DcTerminal> getConnectedDcTerminalStream();

    /**
     * Get the DC grounds connected.
     */
    Iterable<DcGround> getDcGrounds();

    /**
     * Get the DC grounds connected.
     */
    Stream<DcGround> getDcGroundStream();

    /**
     * Get the DC lines connected.
     */
    Iterable<DcLine> getDcLines();

    /**
     * Get the DC lines connected.
     */
    Stream<DcLine> getDcLineStream();

    /**
     * Get the Line Commutated Converters connected.
     */
    Iterable<LineCommutatedConverter> getLineCommutatedConverters();

    /**
     * Get the Line Commutated Converters connected.
     */
    Stream<LineCommutatedConverter> getLineCommutatedConverterStream();

    /**
     * Get the Voltage Source Converters connected.
     */
    Iterable<VoltageSourceConverter> getVoltageSourceConverters();

    /**
     * Get the Voltage Source Converters connected.
     */
    Stream<VoltageSourceConverter> getVoltageSourceConverterStream();
}
