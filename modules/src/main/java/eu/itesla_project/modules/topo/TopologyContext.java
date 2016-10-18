/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Identifiable;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.histo.HistoDbClient;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TopologyContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologyContext.class);

    private final TopologyHistory topologyHistory;

    private final TopologyPredictionContext topologyPredictionContext;

    public TopologyContext(TopologyHistory topologyHistory, TopologyPredictionContext topologyPredictionContext) {
        Objects.requireNonNull(topologyHistory);
        Objects.requireNonNull(topologyPredictionContext);
        this.topologyHistory = topologyHistory;
        this.topologyPredictionContext = topologyPredictionContext;
    }

    public TopologyHistory getTopologyHistory() {
        return topologyHistory;
    }

    public TopologyPredictionContext getTopologyPredictionContext() {
        return topologyPredictionContext;
    }

    public void save(Path dir) {
        topologyHistory.save(dir);
        topologyPredictionContext.save(dir);
    }


    public static Path createTopoCacheDir(Network network, Interval histoInterval, double correlationThreshold, double probabilityThreshold) throws IOException {
        return PlatformConfig.defaultCacheManager().newCacheEntry("topo")
                .withKey(histoInterval.toString())
                .withKey(Double.toString(correlationThreshold))
                .withKey(Double.toString(probabilityThreshold))
                .withKeys(StreamSupport.stream(network.getVoltageLevels().spliterator(), false)
                        .map(Identifiable::getId)
                        .sorted()
                        .collect(Collectors.toList()))
                .build()
                .create();
    }

    public static TopologyContext create(Network network, TopologyMiner topologyMiner, HistoDbClient histoDbClient, ComputationManager computationManager,
                                         Interval histoInterval, double correlationThreshold, double probabilityThreshold) throws IOException {
        // create topo cache dir
        Path topoCacheDir = createTopoCacheDir(network, histoInterval, correlationThreshold, probabilityThreshold);

        // query topology history
        TopologyContext topologyContext = topologyMiner.loadContext(topoCacheDir,
                histoInterval,
                correlationThreshold,
                probabilityThreshold);
        if (topologyContext == null) {
            LOGGER.info("No cached topology context found");
            topologyContext = topologyMiner.initContext(network,
                    histoInterval,
                    correlationThreshold,
                    probabilityThreshold,
                    histoDbClient,
                    computationManager);
            topologyContext.save(topoCacheDir);
        }

        return topologyContext;
    }

}
