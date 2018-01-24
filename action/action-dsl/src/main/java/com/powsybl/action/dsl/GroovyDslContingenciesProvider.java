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

    interface ReaderSupplier {
        Reader getReader() throws IOException;
    }

    private final ReaderSupplier dslReaderSupplier;

    /**
     * Creates a provider which will read the DSL content from a UTF-8 encoded file.
     */
    public static GroovyDslContingenciesProvider fromFile(final Path path) {
        return new GroovyDslContingenciesProvider(()-> Files.newBufferedReader(path, StandardCharsets.UTF_8));
    }

    /**
     * Creates a provider which will read the DSL content from a UTF-8 encoded input stream.
     */
    public static GroovyDslContingenciesProvider fromInputStream(final InputStream input) {
        return new GroovyDslContingenciesProvider(()-> new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    public GroovyDslContingenciesProvider(ReaderSupplier supplier) {
        this.dslReaderSupplier = Objects.requireNonNull(supplier);
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        try (Reader reader = dslReaderSupplier.getReader()) {
            ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(reader, "script", GroovyShell.DEFAULT_CODE_BASE))
                    .load(network);
            return ImmutableList.copyOf(actionDb.getContingencies());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
