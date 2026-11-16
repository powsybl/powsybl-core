/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(Exporter.class)
public class TestExporter implements Exporter {

    @Override
    public String getFormat() {
        return "TST";
    }

    @Override
    public String getComment() {
        return "Dummy Exporter to test Exporters";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        try (OutputStream outputStream = dataSource.newOutputStream(null, "tst", false)) {
            outputStream.write(Byte.BYTES);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
