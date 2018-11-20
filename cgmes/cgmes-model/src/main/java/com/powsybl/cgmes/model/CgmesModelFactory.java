/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreException;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public final class CgmesModelFactory {

    private CgmesModelFactory() {
    }

    public static CgmesModelTripleStore create(ReadOnlyDataSource dataSource, String tripleStoreImpl) {
        TripleStore tripleStore = TripleStoreFactory.create(tripleStoreImpl);
        CimArchive archive = CimArchive.loadOrThrowException(dataSource);
        CgmesModelTripleStore cgmes = new CgmesModelTripleStore(archive.cimNamespace(), tripleStore);
        read(cgmes, dataSource, archive);
        return cgmes;
    }

    private static void read(CgmesModelTripleStore cgmes, ReadOnlyDataSource dataSource, CimArchive fileNames) {
        for (String name : fileNames.getAll()) {
            LOG.info("Reading [{}]", name);
            try (InputStream is = dataSource.newInputStream(name)) {
                cgmes.read("http://default-cgmes-model", name, is);
            } catch (IOException e) {
                String msg = String.format("Reading [%s]", name);
                LOG.warn(msg);
                throw new TripleStoreException(msg, e);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CgmesModelFactory.class);
}
