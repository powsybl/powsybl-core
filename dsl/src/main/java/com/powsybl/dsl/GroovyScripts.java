/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dsl;

import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public final class GroovyScripts {

    private GroovyScripts() {
    }

    public static GroovyCodeSource load(InputStream inputStream) {
        Objects.requireNonNull(inputStream);
        return new GroovyCodeSource(new InputStreamReader(inputStream, StandardCharsets.UTF_8), "script", GroovyShell.DEFAULT_CODE_BASE);
    }

    public static GroovyCodeSource load(Path path) {
        Objects.requireNonNull(path);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new GroovyCodeSource(reader, "script", GroovyShell.DEFAULT_CODE_BASE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
