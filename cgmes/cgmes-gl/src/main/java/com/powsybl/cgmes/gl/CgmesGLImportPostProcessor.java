/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
@AutoService(CgmesImportPostProcessor.class)
public class CgmesGLImportPostProcessor implements CgmesImportPostProcessor {

    private static final String NAME = "cgmesGLImport";
    private static final Logger LOG = LoggerFactory.getLogger(CgmesGLImportPostProcessor.class);

    private final QueryCatalog queryCatalog;

    CgmesGLImportPostProcessor(QueryCatalog queryCatalog) {
        this.queryCatalog = Objects.requireNonNull(queryCatalog);
    }

    public CgmesGLImportPostProcessor() {
        this(new QueryCatalog("CGMES-GL.sparql"));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, TripleStore tripleStore) {
        LOG.info("Execute {} CGMES import post processor on network {}", getName(), network.getId());
        CgmesGLModel cgmesGLModel = new CgmesGLModel(tripleStore, queryCatalog);
        new CgmesGLImporter(network, cgmesGLModel).importGLData();
    }

}
