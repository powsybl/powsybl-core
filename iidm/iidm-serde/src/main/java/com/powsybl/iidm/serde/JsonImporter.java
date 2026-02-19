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
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Properties;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@AutoService(Importer.class)
public class JsonImporter extends AbstractTreeDataImporter {

    private static final String[] EXTENSIONS = {"jiidm", "json"};

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

    protected boolean exists(ReadOnlyDataSource dataSource, String ext) throws IOException {
        if (ext == null) {
            return false;
        }

        try (InputStream is = dataSource.newInputStream(null, ext)) {
            // check the first element is START_OBJECT and second element 'version' key
            try (JsonParser parser = JsonUtil.createJsonFactory().createParser(is)) {
                if (parser.nextToken() != JsonToken.START_OBJECT) {
                    return false;
                }
                String fieldName = parser.nextFieldName();
                if (!JsonReader.VERSION_NAME.equals(fieldName)) {
                    return false;
                }
                parser.nextToken();
                String version = parser.getValueAsString();
                if (version == null || version.isEmpty()) {
                    return false;
                }
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory,
                              Properties parameters, ReportNode reportNode) {
        try {
            String ext = findExtension(dataSource);
            String version = readRootVersion(dataSource, ext);
            IidmVersion.of(version);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return super.importData(dataSource, networkFactory, parameters, reportNode);
    }

    private String readRootVersion(ReadOnlyDataSource ds, String ext) throws IOException {
        try (InputStream is = ds.newInputStream(null, ext)) {
            JsonReader reader = new JsonReader(is, "network", new HashMap<>());
            return reader.readRootVersion();
        }
    }

    @Override
    protected ImportOptions createImportOptions(Properties parameters) {
        return super.createImportOptions(parameters)
                .setFormat(TreeDataFormat.JSON);
    }
}
