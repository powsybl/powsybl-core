/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
@AutoService(AmplExtensionWriter.class)
public class FooNetworkExtensionWriter implements AmplExtensionWriter {

    @Override
    public String getName() {
        return "FooNetwork";
    }

    @Override
    public void write(List<AmplExtension> extensions, Network network, int variantIndex,
                      StringToIntMapper<AmplSubset> mapper, DataSource dataSource, boolean append,
                      AmplExportConfig config) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("foo-network-extension", "txt", false), StandardCharsets.UTF_8)) {
            for (AmplExtension<FooNetworkExtension, Network> ext : extensions) {
                writer.write(ext.getExtendedNum() + " " + ext.getExtension().getName() + "\n");
            }
        }

    }

}
