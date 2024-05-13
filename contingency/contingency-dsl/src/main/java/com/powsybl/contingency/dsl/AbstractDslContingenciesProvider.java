/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.dsl;

import com.powsybl.contingency.ContingenciesProvider;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public abstract class AbstractDslContingenciesProvider implements ContingenciesProvider {

    protected final GroovyCodeSource script;

    /**
     * Creates a provider by reading the DSL from a UTF-8 encoded file.
     */
    protected AbstractDslContingenciesProvider(final Path path) {
        Objects.requireNonNull(path);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            script = new GroovyCodeSource(reader, "script", GroovyShell.DEFAULT_CODE_BASE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Creates a provider by reading the DSL content from a UTF-8 encoded input stream.
     */
    protected AbstractDslContingenciesProvider(final InputStream input) {
        Objects.requireNonNull(input);
        script = new GroovyCodeSource(new InputStreamReader(input, StandardCharsets.UTF_8), "script", GroovyShell.DEFAULT_CODE_BASE);
    }

    @Override
    public String asScript() {
        return script.getScriptText();
    }
}
