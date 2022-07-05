/**
 * Copyright (c) 2019-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public final class NetworkDiagramData extends AbstractExtension<Network> {

    static final String NAME = "network-diagram-data";
    private Map<String, Set<String>> diagramsNames = new TreeMap<>();

    private NetworkDiagramData() {
    }

    private static NetworkDiagramData getNetworkDiagramData(Network network) {
        NetworkDiagramData networkDiagramData = network.getExtension(NetworkDiagramData.class);
        if (networkDiagramData == null) {
            networkDiagramData = new NetworkDiagramData();
        }
        return networkDiagramData;
    }

    public static void addDiagramName(Network network, String diagramName, String substation) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(diagramName);
        NetworkDiagramData networkDiagramData = getNetworkDiagramData(network);
        networkDiagramData.addDiagramName(diagramName, substation);
        network.addExtension(NetworkDiagramData.class, networkDiagramData);
    }

    public static List<String> getDiagramsNames(Network network) {
        Objects.requireNonNull(network);
        return getNetworkDiagramData(network).getDiagramsNames();
    }

    public static boolean checkNetworkDiagramData(Network network) {
        Objects.requireNonNull(network);
        return network.getExtension(NetworkDiagramData.class) != null;
    }

    public static boolean containsDiagramName(Network network, String diagramName) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(diagramName);
        return checkNetworkDiagramData(network) && getNetworkDiagramData(network).diagramsNames.keySet().contains(diagramName);
    }

    public static List<String> getSubstations(Network network, String diagramName) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(diagramName);
        return getNetworkDiagramData(network).getSubstations(diagramName);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private void addDiagramName(String diagramName, String substation) {
        Set<String> substations = diagramsNames.getOrDefault(diagramName, new TreeSet<>());
        substations.add(substation);
        diagramsNames.put(diagramName, substations);
    }

    private List<String> getDiagramsNames() {
        return new ArrayList<>(diagramsNames.keySet());
    }

    private List<String> getSubstations(String diagramName) {
        return new ArrayList<>(diagramsNames.getOrDefault(diagramName, new TreeSet<>()));
    }
}
