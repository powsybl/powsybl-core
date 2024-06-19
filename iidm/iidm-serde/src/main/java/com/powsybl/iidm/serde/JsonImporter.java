/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.commons.json.JsonReader;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@AutoService(Importer.class)
public class JsonImporter extends AbstractTreeDataImporter {

    private static final String[] EXTENSIONS = {"jiidm", "json", "iidm.json"};

    @Override
    protected String[] getExtensions() {
        return EXTENSIONS;
    }

    @Override
    public String getFormat() {
        return "JIIDM";
    }

    @Override
    public String getComment() {
        return "IIDM JSON v " + CURRENT_IIDM_VERSION.toString(".") + " importer";
    }

    @Override
    protected boolean exists(ReadOnlyDataSource dataSource, String ext) throws IOException {
        if (ext != null) {
            try (InputStream is = dataSource.newInputStream(null, ext)) {
                // check the first element is START_OBJECT and second element 'version' key
                try (JsonParser parser = JsonUtil.createJsonFactory().createParser(is)) {
                    if (parser.nextToken() != JsonToken.START_OBJECT) {
                        return false;
                    }
                    return JsonReader.VERSION_NAME.equals(parser.nextFieldName());
                }
            }
        }
        return false;
    }

    @Override
    protected ImportOptions createImportOptions(Properties parameters) {
        return super.createImportOptions(parameters)
                .setFormat(TreeDataFormat.JSON);
    }
}
