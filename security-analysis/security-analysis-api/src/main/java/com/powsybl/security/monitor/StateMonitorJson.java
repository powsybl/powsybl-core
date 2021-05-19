/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public final class StateMonitorJson {

    private StateMonitorJson() {
    }

    public static void write(List<StateMonitor> monitors, Path jsonFile) {
        try {
            OutputStream out = Files.newOutputStream(jsonFile);
            JsonUtil.createObjectMapper().writer().writeValue(out, monitors);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<StateMonitor> read(Path jsonFile) {
        try {
            return JsonUtil.createObjectMapper().readerFor(new TypeReference<List<StateMonitor>>() {
            }).readValue(Files.newInputStream(jsonFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
