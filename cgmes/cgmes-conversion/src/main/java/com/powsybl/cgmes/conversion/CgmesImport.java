package com.powsybl.cgmes.conversion;

/*
 * #%L
 * CGMES conversion
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.CgmesModel;
import com.powsybl.cgmes.CgmesModelFactory;
import com.powsybl.cgmes.CgmesOnDataSource;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Importer.class)
public class CgmesImport implements Importer {

    @Override
    public boolean exists(ReadOnlyDataSource ds) {
        CgmesOnDataSource cds = new CgmesOnDataSource(ds);
        if (cds.exists()) {
            return true;
        }
        // If we are configured to support CIM14,
        // check if there is this CIM14 data
        return importCim14 && cds.existsCim14();
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public Network importData(ReadOnlyDataSource ds, Properties p) {
        CgmesModel cgmes = CgmesModelFactory.create(ds, tripleStoreImpl(p));

        Conversion.Config config = new Conversion.Config();
        if (p != null && p.containsKey("changeSignForShuntReactivePowerFlowInitialState")) {
            String s = p.getProperty("changeSignForShuntReactivePowerFlowInitialState");
            Boolean b = Boolean.parseBoolean(s);
            config.setChangeSignForShuntReactivePowerFlowInitialState(b);
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

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        throw new UnsupportedOperationException("Pending implementation");
    }

    private String tripleStoreImpl(Properties p) {
        if (p == null) {
            return TripleStoreFactory.defaultImplementation();
        }
        return p.getProperty("powsyblTripleStore", TripleStoreFactory.defaultImplementation());
    }

    private static final String FORMAT                 = "CGMES";
    public static final String  NETWORK_PS_CGMES_MODEL = "CGMESModel";

    // FIXME Allow this property to be configurable
    // Parameters of importers are only passed to importData method,
    // but to decide if we are importers also for CIM 14 files
    // we must implement the exists method, that has not access to parameters
    private boolean             importCim14            = false;

    private static final Logger LOG                    = LoggerFactory.getLogger(CgmesImport.class);
}
