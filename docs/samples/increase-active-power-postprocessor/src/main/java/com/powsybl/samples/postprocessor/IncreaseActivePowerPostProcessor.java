/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.samples.postprocessor;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.ImportPostProcessor;
import com.powsybl.iidm.network.Network;

@AutoService(ImportPostProcessor.class)
public class IncreaseActivePowerPostProcessor implements ImportPostProcessor {

    public static final String NAME = "increaseActivePower";
    private static final Logger LOGGER = LoggerFactory.getLogger(IncreaseActivePowerPostProcessor.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, ComputationManager computationManager) throws Exception {
        Objects.requireNonNull(network);
        LOGGER.info("Execute {} post processor on network {}", getName(), network.getId());
        double percent = 1.01;
        network.getLoads().forEach(load -> {
            load.setP0(load.getP0() * percent);
            double p = load.getTerminal().getP();
            load.getTerminal().setP(p * percent);
            LOGGER.info("Load {} : p {} -> {}", load.getId(), p, load.getTerminal().getP());
        });
    }

}
