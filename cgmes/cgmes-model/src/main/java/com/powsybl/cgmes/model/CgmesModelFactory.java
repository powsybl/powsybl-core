/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreException;
import com.powsybl.triplestore.api.TripleStoreFactory;

public final class CgmesModelFactory {

    private CgmesModelFactory() {
    }

    public static CgmesModelTripleStore create(ReadOnlyDataSource ds, String tripleStoreImpl) {
        CgmesOnDataSource cds = new CgmesOnDataSource(ds);
        TripleStore tripleStore = TripleStoreFactory.create(tripleStoreImpl);
        CgmesModelTripleStore cgmes = new CgmesModelTripleStore(cds.cimNamespace(), tripleStore);
        read(cgmes, cds);
        return cgmes;
    }

    private static void read(CgmesModelTripleStore cgmes, CgmesOnDataSource cds) {
        String base = cds.baseName();
        for (String name : cds.names()) {
            LOG.info("Reading [{}]", name);
            try (InputStream is = cds.dataSource().newInputStream(name)) {
                cgmes.read(base, name, is);
            } catch (IOException e) {
                String msg = String.format("Reading [%s]", name);
                LOG.warn(msg);
                throw new TripleStoreException(msg, e);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CgmesModelFactory.class);
}
