/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.google.common.collect.ImmutableList;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroovyDslContingenciesProvider implements ContingenciesProvider {


    private final ActionDslLoader dslLoader;

    /**
     * Creates a provider by reading the DSL from a UTF-8 encoded file.
     */
    public GroovyDslContingenciesProvider(final Path path) {
        Objects.requireNonNull(path);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            this.dslLoader = createLoader(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Creates a provider by reading the DSL content from a UTF-8 encoded input stream.
     */
    public  GroovyDslContingenciesProvider(final InputStream input) {
        Objects.requireNonNull(input);
        this.dslLoader = createLoader(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    private static ActionDslLoader createLoader(final Reader reader) {
        GroovyCodeSource src = new GroovyCodeSource(reader, "script", GroovyShell.DEFAULT_CODE_BASE);
        return new ActionDslLoader(src);
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        ActionDb actionDb = dslLoader.load(network);
        return ImmutableList.copyOf(actionDb.getContingencies());
    }

}
