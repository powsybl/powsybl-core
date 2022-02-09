/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(Importer.class)
public class TestImporter implements Importer {

    @Override
    public String getFormat() {
        return "TST";
    }

    @Override
    public String getComment() {
        return "Dummy importer to test Importers";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            return dataSource == null || dataSource.exists(null, "tst");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, Reporter reporter) {
        if (reporter != null) {
            reporter.report("test", "Import model ${model}", "model", "eurostagTutorialExample1");
        }
        return EurostagTutorialExample1Factory.create();
    }
}
