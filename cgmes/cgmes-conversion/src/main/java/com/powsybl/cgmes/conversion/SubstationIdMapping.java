/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesModel.CgmesTerminal;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SubstationIdMapping {

    public SubstationIdMapping(Conversion.Context context) {
        this.context = context;
        this.mapping = new HashMap<>();
    }

    public boolean isMapped(String cgmesIdentifier) {
        String sid = context.namingStrategy().getId(CgmesNames.SUBSTATION, cgmesIdentifier);
        return mapping.containsKey(sid);
    }

    public String iidm(String cgmesIdentifier) {
        String sid = context.namingStrategy().getId(CgmesNames.SUBSTATION, cgmesIdentifier);
        if (mapping.containsKey(sid)) {
            return mapping.get(sid);
        }
        return sid;
    }

    public void build() {
        // CGMES standard:
        // "a PowerTransformer is contained in one Substation but it can connect a Terminal to
        // another Substation"
        // Ends of transformers need to be in the same substation in the IIDM model.
        // We will map some CGMES substations to a single IIDM substation
        // when they are connected by transformers,
        // that is, when there are at least one power transformer that has terminals on both
        // substations

        UndirectedGraph<String, Object> g = graphSubstationsTransformers();
        new ConnectivityInspector<>(g).connectedSets().stream()
                .filter(substationIds -> substationIds.size() > 1)
                .forEach(substationIds -> {
                    String selectedSubstationId = representativeSubstationId(substationIds);
                    for (String substationId : substationIds) {
                        if (!substationId.equals(selectedSubstationId)) {
                            mapping.put(substationId, selectedSubstationId);
                        }
                    }
                });
        if (!mapping.isEmpty()) {
            LOG.warn("Substation id mapping needed for {} substations: {}",
                    mapping.size(), mapping);
        }
    }

    private String representativeSubstationId(Collection<String> substationIds) {
        return substationIds.stream()
                .filter(substationId -> context.config().substationIdsExcludedFromMapping()
                        .stream()
                        .noneMatch(substationId::matches))
                .sorted()
                .findFirst()
                .orElse(substationIds.iterator().next());
    }

    private UndirectedGraph<String, Object> graphSubstationsTransformers() {
        UndirectedGraph<String, Object> graph = new Pseudograph<>(Object.class);
        for (PropertyBag s : context.cgmes().substations()) {
            String id = s.getId(CgmesNames.SUBSTATION);
            graph.addVertex(context.namingStrategy().getId(CgmesNames.SUBSTATION, id));
        }
        for (PropertyBags tends : context.cgmes().groupedTransformerEnds().values()) {
            List<String> substationsIds = substationsIds(tends);
            if (substationsIds.size() > 1) {
                for (int i = 1; i < substationsIds.size(); i++) {
                    graph.addEdge(substationsIds.get(0), substationsIds.get(i));
                }
            }
        }
        return graph;
    }

    private List<String> substationsIds(PropertyBags tends) {
        List<String> substationsIds = new ArrayList<>();
        for (PropertyBag end : tends) {
            CgmesTerminal t = context.cgmes().terminal(end.getId(CgmesNames.TERMINAL));
            String node = context.nodeBreaker() ? t.connectivityNode() : t.topologicalNode();
            if (node != null && !context.boundary().containsNode(node)) {
                String sid = t.substation();
                substationsIds.add(context.namingStrategy().getId(CgmesNames.SUBSTATION, sid));
            }
        }
        return substationsIds;
    }

    private final Conversion.Context context;
    private final Map<String, String> mapping;

    private static final Logger LOG = LoggerFactory.getLogger(SubstationIdMapping.class);
}
