/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableList;
import com.powsybl.action.json.ActionJsonModule;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * A container for a list of actions.
 * The JSON representation of this object is versioned.
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ActionList {
    protected final List<Action> actions;
    public static final String VERSION = "1.2";

    public ActionList(List<Action> actions) {
        this.actions = ImmutableList.copyOf(Objects.requireNonNull(actions));
    }

    public List<Action> getActions() {
        return actions;
    }

    public static ActionList readJsonFile(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return readJsonInputStream(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ActionList readJsonInputStream(InputStream is) {
        Objects.requireNonNull(is);
        try {
            return createObjectMapper().readValue(is, ActionList.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Writes action list as JSON to a file.
     */
    public void writeJsonFile(Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (OutputStream outputStream = Files.newOutputStream(jsonFile)) {
            this.writeJsonOutputStream(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Writes action list as JSON to an output stream.
     */
    public void writeJsonOutputStream(OutputStream outputStream) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(outputStream, this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new ActionJsonModule());
    }
}
