/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;

/**
*
* @author Ferrari Giovanni <giovanni.ferrari@techrain.eu>
*/
@AutoService(AmplExtensionWriter.class)
public class FooExtensionWriter implements AmplExtensionWriter {

    @Override
    public String getName() {
        return "Foo";
    }

    @Override
    public void write(List<AmplExtension> extensions, Network network,
            StringToIntMapper<AmplSubset> mapper, DataSource dataSource, boolean append,
            AmplExportConfig config) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("foo-extension", "txt", false), StandardCharsets.UTF_8)) {
            for (AmplExtension<FooExtension, HvdcLine> ext : extensions) {
                writer.write(ext.getExtendedNum() + " " + ext.getExtension().getName() + "\n");
            }
        }

    }


}
