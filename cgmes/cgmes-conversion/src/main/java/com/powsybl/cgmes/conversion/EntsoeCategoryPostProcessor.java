/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategoryAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.TripleStore;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm@aia.es>}
 */
@AutoService(CgmesImportPostProcessor.class)
public class EntsoeCategoryPostProcessor implements CgmesImportPostProcessor {

    public static final String NAME = "EntsoeCategory";
    private static final Logger LOG = LoggerFactory.getLogger(EntsoeCategoryPostProcessor.class);

    public EntsoeCategoryPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public EntsoeCategoryPostProcessor(PlatformConfig platformConfig) {
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
        for (PropertyBag sm : network.getExtension(CgmesModelExtension.class).getCgmesModel().synchronousMachinesGenerators()) {
            String generatingUnitId = sm.getId("GeneratingUnit");
            if (generatingUnitId != null) {
                processGenerator(network, sm, generatingUnitId);
            }
        }
    }

    private static void processGenerator(Network network, PropertyBag sm, String generatingUnitId) {
        String description = sm.getId("generatingUnitDescription");
        // String contains only digits or spaces
        if (!StringUtils.isNumericSpace(description)) {
            return;
        }
        String description1 = description.trim();
        if (description1.isEmpty()) {
            return;
        }
        String generatorId = sm.getId("SynchronousMachine");
        Generator g = network.getGenerator(generatorId);
        if (g == null) {
            return;
        }
        try {
            int code = Integer.parseInt(description1);
            if (code > 0) {
                g.newExtension(GeneratorEntsoeCategoryAdder.class)
                        .withCode(code)
                        .add();
            }
        } catch (Exception x) {
            LOG.error("Bad number for ENTSO-E category from description [{}], generating Unit: {}, generator: {}", description, generatingUnitId, generatorId);
        }
    }
}
