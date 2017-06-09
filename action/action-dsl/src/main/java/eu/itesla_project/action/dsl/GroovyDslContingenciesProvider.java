/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.dsl;

import com.google.common.collect.ImmutableList;
import eu.itesla_project.contingency.ContingenciesProvider;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.iidm.network.Network;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroovyDslContingenciesProvider implements ContingenciesProvider {

    private final Path dslFile;

    public GroovyDslContingenciesProvider(Path dslFile) {
        this.dslFile = Objects.requireNonNull(dslFile);
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        try (Reader reader = Files.newBufferedReader(dslFile, StandardCharsets.UTF_8)) {
            ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(reader, "script", GroovyShell.DEFAULT_CODE_BASE))
                    .load(network);
            return ImmutableList.copyOf(actionDb.getContingencies());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
