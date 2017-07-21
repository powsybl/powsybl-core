/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.core;

import com.google.common.collect.ImmutableList;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.datasource.ReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.parameters.Parameter;
import eu.itesla_project.iidm.parameters.ParameterType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestImporter implements Importer {

    public static final String FORMAT = "TEST";
    public static final String EXT = "tst";

    private final Network network;

    public TestImporter(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public String getComment() {
        return "Test format";
    }

    @Override
    public InputStream get16x16Icon() {
        return new ByteArrayInputStream(new byte[16 * 16]);
    }

    @Override
    public List<Parameter> getParameters() {
        return ImmutableList.of(new Parameter("param1", ParameterType.BOOLEAN, "", Boolean.TRUE),
                new Parameter("param2", ParameterType.STRING, "", "value"));
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            return dataSource.exists(null, EXT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network import_(ReadOnlyDataSource dataSource, Properties parameters) {
        return network;
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
    }
}
