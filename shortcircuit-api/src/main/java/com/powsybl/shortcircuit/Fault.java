/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.json.ShortCircuitAnalysisJsonModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface to describe the characteristics of the fault to be simulated.
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public interface Fault {

    /**
     * Type of fault (use for downcast & serialize/deserialize).
     */
    enum Type {
        BUS,
        BRANCH
    }

    /**
     * How the fault impedance and resistance are connected to the ground.
     */
    enum ConnectionType {
        SERIES,
        PARALLEL,
    }

    /**
     * The type of fault being simulated.
     */
    enum FaultType {
        THREE_PHASE,
        SINGLE_PHASE,
    }

    //TODO : add the numbers of the phase for two and single phase

    /**
     * The ID of the fault.
     */
    String getId();

    /**
     * The ID of the equipment or bus associated to this fault.
     */
    String getElementId();

    /**
     * The resistance of the fault to the ground. The default is zero Ohms.
     */
    double getRToGround();

    /**
     * The reactance of the fault to the ground. The default is zero Ohms.
     */
    double getXToGround();

    /**
     * The type of the element associated to the fault: can be BUS or BRANCH.
     */
    Type getType();

    /**
     * How the fault resistance and reactance are connected to the ground. Can be SERIES or PARALLEL.
     */
    ConnectionType getConnectionType();

    /**
     * The type of fault occurring on the network element: can be THREE-PHASE or SINGLE-PHASE.
     */
    FaultType getFaultType();

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper().registerModule(new ShortCircuitAnalysisJsonModule());
    }

    /**
     * Writes a list of faults to a JSON file
     * @param faults the list of faults
     * @param jsonFile the path to the JSON file
     */
    static void write(List<Fault> faults, Path jsonFile) {
        try (OutputStream out = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, faults);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads a JSON file and creates the associated list of faults
     * @param jsonFile the path to the existing JSON file
     * @return a list of faults
     */
    static List<Fault> read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return createObjectMapper().readerForListOf(Fault.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
