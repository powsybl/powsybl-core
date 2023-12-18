/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.modification.util.VoltageRegulationUtils;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

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
                      ComputationManager computationManager, Reporter reporter) {
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
     * Checks if generator is connected to the network with transformers and connecting them.
     * (If no transformer exist, nothing is modified)
     */
    private static void connectTransformersOfGenerator(Generator generator) {
        ConnectableTopologyVisitor connectableTopologyVisitor = new ConnectableTopologyVisitor();
        generator.getTerminal().getBusBreakerView().getConnectableBus().visitConnectedOrConnectableEquipments(connectableTopologyVisitor);
        if (generatorIsOnlyConnectedToTransformers(generator, connectableTopologyVisitor.getConnectables())) {
            connectableTopologyVisitor.getConnectables().stream().filter(connectable -> connectable.getType().equals(IdentifiableType.TWO_WINDINGS_TRANSFORMER))
                    .map(c -> (TwoWindingsTransformer) c)
                    .forEach(twt -> {
                        LOGGER.info("Connecting twoWindingsTransformer {} linked to generator {}", twt.getId(), generator.getId());
                        twt.getTerminals().forEach(Terminal::connect);
                    });

        }
    }

    private static boolean generatorIsOnlyConnectedToTransformers(Generator generator, List<Connectable> connectables) {
        List<Connectable> otherConnectable = connectables.stream().filter(connectable -> !connectable.getNameOrId().equals(generator.getNameOrId()) && !connectable.getType().equals(IdentifiableType.TWO_WINDINGS_TRANSFORMER)).toList();
        return otherConnectable.isEmpty();
    }

}
