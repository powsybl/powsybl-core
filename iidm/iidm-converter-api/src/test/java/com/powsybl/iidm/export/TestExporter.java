/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(Exporter.class)
public class TestExporter implements Exporter {

    @Override
    public String getFormat() {
        return "TST";
    }

    @Override
    public String getComment() {
        return "A test exporter";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream(null, "tst", false))) {
            writer.write("This is a test");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
