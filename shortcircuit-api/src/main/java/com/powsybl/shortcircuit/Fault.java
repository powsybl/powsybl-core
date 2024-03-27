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
 * Used for elementary short-circuit analysis only.
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public interface Fault {

    // Type of fault (use for downcast & serialize/deserialize)
    enum Type {
        BUS,
        BRANCH
    }

    // How the fault impedance and resistance are associated.
    enum ConnectionType {
        SERIES,
        PARALLEL,
    }

    // What kind of fault is simulated
    enum FaultType {
        THREE_PHASE,
        SINGLE_PHASE,
    }

    //TODO : add the numbers of the phase for two and single phase

    // The fault id.
    String getId();

    // The equipment or bus id where the fault is simulated.
    String getElementId();

    // Characteristics of the short circuit to ground.
    double getRToGround();

    double getXToGround();

    Type getType();

    ConnectionType getConnectionType();

    FaultType getFaultType();

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper().registerModule(new ShortCircuitAnalysisJsonModule());
    }

    static void write(List<Fault> faults, Path jsonFile) {
        try (OutputStream out = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, faults);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<Fault> read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return createObjectMapper().readerForListOf(Fault.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
