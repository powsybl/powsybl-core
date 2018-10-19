/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterType;

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
    public List<Parameter> getParameters() {
        return ImmutableList.of(new Parameter("param1", ParameterType.BOOLEAN, "", Boolean.TRUE),
                new Parameter("param2", ParameterType.STRING, "", "value"));
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        return dataSource.getMainFileName() != null
                && dataSource.getMainFileName().endsWith(EXT)
                && dataSource.fileExists(dataSource.getMainFileName());
    }

    @Override
    public String getPrettyName(ReadOnlyDataSource dataSource) {
        return "network";
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, Properties parameters) {
        return network;
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
    }
}
