/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.dot;

import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class Subgraph<V> {

    public static final Map<String, Attribute> DEFAULT_SUBGRAPH_ATTRIBUTES = Map.of(
        "pencolor", DefaultAttribute.createAttribute("transparent"));
    public static final Map<String, Attribute> DEFAULT_CLUSTER_ATTRIBUTES = Map.of(
        "label", DefaultAttribute.createAttribute(""),
        "shape", DefaultAttribute.createAttribute("point"),
        "style", DefaultAttribute.createAttribute("invis"));

    private final Set<V> vertices;
    private final Map<String, Attribute> subgraphAttributes;
    private final Map<String, Attribute> clusterAttributes;

    public Subgraph() {
        this.vertices = new HashSet<>();
        this.subgraphAttributes = new HashMap<>();
        this.clusterAttributes = new HashMap<>();
    }

    public Subgraph(Map<String, Attribute> subgraphAttributes, Map<String, Attribute> clusterAttributes) {
        this.vertices = new HashSet<>();
        this.subgraphAttributes = new HashMap<>(subgraphAttributes);
        this.clusterAttributes = new HashMap<>(clusterAttributes);
    }

    public Set<V> getVertices() {
        return vertices;
    }

    public Map<String, Attribute> getSubgraphAttributes() {
        return subgraphAttributes;
    }

    public Map<String, Attribute> getClusterAttributes() {
        return clusterAttributes;
    }

    public void addVertex(V vertex) {
        vertices.add(vertex);
    }
}
