/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.modification.util.VoltageRegulationUtils;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.alg.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class ConnectGenerator extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectGenerator.class);

    private final String generatorId;

    public ConnectGenerator(String generatorId) {
        this.generatorId = generatorId;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException,
                      ComputationManager computationManager, ReportNode reportNode) {
        Generator g = network.getGenerator(generatorId);
        if (g == null) {
            throw new PowsyblException("Generator '" + generatorId + "' not found");
        }

        connect(g);
    }

    static void connect(Generator g) {
        Objects.requireNonNull(g);

        if (g.getTerminal().isConnected()) {
            return;
        }

        Terminal t = g.getTerminal();
        t.connect();
        if (g.isVoltageRegulatorOn()) {
            VoltageRegulationUtils.getTargetVForRegulatingElement(g.getNetwork(), g.getRegulatingTerminal().getBusView().getBus(), g.getId(), IdentifiableType.GENERATOR)
                    .ifPresent(g::setTargetV);
        }
        connectTransformersOfGenerator(g);
    }

    /**
     * Checks if the generator is connected to the network through transformers and, if it is the case, connect them too.
     * If it is not the case, nothing is modified
     */
    private static void connectTransformersOfGenerator(Generator generator) {
        Bus genBus = getBus(generator.getTerminal());
        Set<TwoWindingsTransformer> transformers = generator.getTerminal().getVoltageLevel().getTwoWindingsTransformerStream()
                .filter(twt -> genBus.equals(getBus(twt.getTerminal1())) || genBus.equals(getBus(twt.getTerminal2())))
                .collect(Collectors.toSet());
        Map<String, Pair<Boolean, Boolean>> twtConnectionInitialState = new HashMap<>();
        transformers.forEach(twt -> {
            LOGGER.info("Connecting twoWindingsTransformer {} linked to generator {}", twt.getId(), generator.getId());
            twtConnectionInitialState.put(twt.getId(), Pair.of(twt.getTerminal1().isConnected(), twt.getTerminal2().isConnected()));
            twt.getTerminals().forEach(Terminal::connect);
        });
        // If, even after connecting the transformer, the generator remains disconnected from the main component, we remove the unnecessary transformer connection.
        if (!generator.getTerminal().getBusBreakerView().getConnectableBus().isInMainConnectedComponent()) {
            revertInitialState(generator, transformers, twtConnectionInitialState);
        }
    }

    private static void revertInitialState(Generator generator, Set<TwoWindingsTransformer> transformers, Map<String, Pair<Boolean, Boolean>> twtConnectionInitialState) {
        transformers.forEach(twt -> {
            LOGGER.info("Generator {} could not be connected to the main component, reset initial status for twoWindingsTransformer {}", generator.getId(), twt.getId());
            Pair<Boolean, Boolean> initialTerminalStatus = twtConnectionInitialState.get(twt.getId());
            applyTerminalStatus(twt.getTerminal1(), initialTerminalStatus.getFirst());
            applyTerminalStatus(twt.getTerminal2(), initialTerminalStatus.getSecond());
        });
    }

    private static void applyTerminalStatus(Terminal terminal, boolean connect) {
        if (connect) {
            terminal.connect();
        } else {
            terminal.disconnect();
        }

    }

    private static Bus getBus(Terminal terminal) {
        return terminal.getBusBreakerView().getConnectableBus();
    }

}
