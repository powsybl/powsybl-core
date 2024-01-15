/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.iidm.network.Importer;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * Binary import of an IIDM model.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@AutoService(Importer.class)
public class BinaryImporter extends AbstractTreeDataImporter {

    private static final String[] EXTENSIONS = {"biidm", "bin", "iidm.bin"};

    @Override
    protected String[] getExtensions() {
        return EXTENSIONS;
    }

    @Override
    public String getFormat() {
        return "BIIDM";
    }

    @Override
    public String getComment() {
        return "IIDM binary v " + CURRENT_IIDM_VERSION.toString(".") + " importer";
    }

    @Override
    protected boolean exists(ReadOnlyDataSource dataSource, String ext) throws IOException {
        if (ext != null) {
            try (DataInputStream dis = new DataInputStream(dataSource.newInputStream(null, ext))) {
                int versionNbBytes = dis.readShort();
                if (versionNbBytes > 0 && versionNbBytes < 50) { // large enough for version string
                    byte[] stringBytes = dis.readNBytes(versionNbBytes);
                    if (stringBytes.length == versionNbBytes) {
                        String version = new String(stringBytes, StandardCharsets.UTF_8);
                        return version.matches("^\\d+_\\d+(_\\d+)?$");
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected ImportOptions createImportOptions(Properties parameters) {
        return super.createImportOptions(parameters)
                .setFormat(TreeDataFormat.BIN);
    }
}
