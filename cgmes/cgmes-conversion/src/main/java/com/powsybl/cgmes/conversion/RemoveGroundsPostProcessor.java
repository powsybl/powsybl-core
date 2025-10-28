/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.modification.topology.RemoveFeederBay;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm@aia.es>}
 */
@AutoService(CgmesImportPostProcessor.class)
public class RemoveGroundsPostProcessor implements CgmesImportPostProcessor {

    public static final String NAME = "RemoveGrounds";
    private static final Logger LOG = LoggerFactory.getLogger(RemoveGroundsPostProcessor.class);

    public RemoveGroundsPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public RemoveGroundsPostProcessor(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, TripleStore tripleStore) {
        Objects.requireNonNull(network);
        LOG.info("Execute {} post processor on network {}", getName(), network.getId());
        List<String> grounds = network.getGroundStream().map(Identifiable::getId).toList();
        grounds.forEach(g -> new RemoveFeederBay(g).apply(network));
    }
}
