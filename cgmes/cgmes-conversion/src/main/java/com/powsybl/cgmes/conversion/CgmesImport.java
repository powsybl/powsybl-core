/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.CimArchive;
import com.powsybl.cgmes.model.Subset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStoreFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Importer.class)
public class CgmesImport implements Importer {

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        return CimArchive.load(dataSource).isPresent();
    }

    @Override
    public String getPrettyName(ReadOnlyDataSource dataSource) {
        return CimArchive.loadOrThrowException(dataSource).baseName();
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        CimArchive fromFileNames = CimArchive.loadOrThrowException(fromDataSource);
        Objects.requireNonNull(toDataSource);
        try {
            for (Subset subset : Subset.values()) {
                for (String fileName : fromFileNames.getFileName(subset)) {
                    copyStream(fromDataSource, toDataSource, fileName, fileName);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void copyStream(ReadOnlyDataSource from, DataSource to, String fromName, String toName) throws IOException {
        if (from.fileExists(fromName)) {
            try (InputStream is = from.newInputStream(fromName);
                 OutputStream os = to.newOutputStream(toName, false)) {
                ByteStreams.copy(is, os);
            }
        }
    }

    private static String tripleStoreImpl(Properties p) {
        if (p == null) {
            return TripleStoreFactory.defaultImplementation();
        }
        return p.getProperty("powsyblTripleStore", TripleStoreFactory.defaultImplementation());
    }

    @Override
    public Network importData(ReadOnlyDataSource ds, Properties p) {
        CgmesModel cgmes = CgmesModelFactory.create(ds, tripleStoreImpl(p));

        Conversion.Config config = new Conversion.Config();
        if (p != null) {
            if (p.containsKey("changeSignForShuntReactivePowerFlowInitialState")) {
                String s = p.getProperty("changeSignForShuntReactivePowerFlowInitialState");
                config.setChangeSignForShuntReactivePowerFlowInitialState(Boolean.parseBoolean(s));
            }
            if (p.containsKey("convertBoundary")) {
                String s = p.getProperty("convertBoundary");
                config.setConvertBoundary(Boolean.parseBoolean(s));
            }
        }
        Network network = new Conversion(cgmes, config).convertedNetwork();

        boolean storeCgmesModelAsNetworkProperty = true;
        if (p != null) {
            storeCgmesModelAsNetworkProperty = Boolean
                    .parseBoolean(p.getProperty("storeCgmesModelAsNetworkProperty", "true"));
        }
        if (storeCgmesModelAsNetworkProperty) {
            // Store a reference to the original CGMES model inside the IIDM network
            // We could also add listeners to be aware of changes in IIDM data
            network.getProperties().put(NETWORK_PS_CGMES_MODEL, cgmes);
        }

        return network;
    }

    private static final String FORMAT = "CGMES";

    public static final String NETWORK_PS_CGMES_MODEL = "CGMESModel";
}
