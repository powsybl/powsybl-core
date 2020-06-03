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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SubstationIdMapping {

    public SubstationIdMapping(Context context) {
        this.context = context;
        this.substationMapping = new HashMap<>();
        this.voltageLevelMapping = new HashMap<>();
    }

    public boolean subStationIsMapped(String cgmesIdentifier) {
        String sid = context.namingStrategy().getId(CgmesNames.SUBSTATION, cgmesIdentifier);
        return substationMapping.containsKey(sid);
    }

    public String substationIidm(String cgmesIdentifier) {
        String sid = context.namingStrategy().getId(CgmesNames.SUBSTATION, cgmesIdentifier);
        if (substationMapping.containsKey(sid)) {
            return substationMapping.get(sid);
        }
        return sid;
    }

    public boolean voltageLevelIsMapped(String cgmesIdentifier) {
        String vlid = context.namingStrategy().getId(CgmesNames.VOLTAGE_LEVEL, cgmesIdentifier);
        return voltageLevelMapping.containsKey(vlid);
    }

    public String voltageLevelIidm(String cgmesIdentifier) {
        String vlid = context.namingStrategy().getId(CgmesNames.VOLTAGE_LEVEL, cgmesIdentifier);
        if (voltageLevelMapping.containsKey(vlid)) {
            return voltageLevelMapping.get(vlid);
        }
        return vlid;
    }

    // CGMES standard:
    // "a PowerTransformer is contained in one Substation but it can connect a Terminal to
    // another Substation"
    // Ends of transformers need to be in the same substation in the IIDM model.
    // We will map some CGMES substations to a single IIDM substation
    // when they are connected by transformers,
    // that is, when there are at least one power transformer that has terminals on both
    // substations
    // Ends of switches need to be in the same voltageLevel in the IIDM model.
    // We will map some CGMES voltageLevels to a single IIDM voltageLevel
    // when they are connected by switches

    public void build() {
        Map<String, List<String>> voltageLevelAdjacency = new HashMap<>();
        Map<String, List<String>> substationAdjacency = new HashMap<>();

        buildAdjacency(voltageLevelAdjacency, substationAdjacency);
        buildVoltageLevel(voltageLevelAdjacency);
        buildSubstation(substationAdjacency);
    }

    private void buildAdjacency(Map<String, List<String>> voltageLevelAdjacency,
        Map<String, List<String>> substationAdjacency) {
        context.cgmes().voltageLevels().forEach(vl -> addVoltageLevel(voltageLevelAdjacency, vl));
        context.cgmes().substations().forEach(st -> addSubstation(substationAdjacency, st));

        context.cgmes().switches().forEach(sw -> addSwitch(voltageLevelAdjacency, substationAdjacency, sw));
        context.cgmes().groupedTransformerEnds().forEach((t, tends) -> addEnds(substationAdjacency, tends));
    }

    private void addVoltageLevel(Map<String, List<String>> voltageLevelAdjacency, PropertyBag vl) {
        String voltageLevelId = vl.getId(CgmesNames.VOLTAGE_LEVEL);
        String vId = context.namingStrategy().getId(CgmesNames.VOLTAGE_LEVEL, voltageLevelId);
        voltageLevelAdjacency.computeIfAbsent(vId, k -> new ArrayList<>());
    }

    private void addSubstation(Map<String, List<String>> substationAdjacency, PropertyBag sub) {
        String substationlId = sub.getId(CgmesNames.SUBSTATION);
        String subId = context.namingStrategy().getId(CgmesNames.SUBSTATION, substationlId);
        substationAdjacency.computeIfAbsent(subId, k -> new ArrayList<>());
    }

    private void addSwitch(Map<String, List<String>> voltageLevelAdjacency,
        Map<String, List<String>> substationAdjacency, PropertyBag sw) {
        CgmesTerminal t1 = context.cgmes().terminal(sw.getId(CgmesNames.TERMINAL + 1));
        String node1 = context.nodeBreaker() ? t1.connectivityNode() : t1.topologicalNode();

        String voltageLevelId1 = nodeGetVoltageLevel(node1, t1);
        if (voltageLevelId1 == null) {
            return;
        }

        CgmesTerminal t2 = context.cgmes().terminal(sw.getId(CgmesNames.TERMINAL + 2));
        String node2 = context.nodeBreaker() ? t2.connectivityNode() : t2.topologicalNode();
        String voltageLevelId2 = nodeGetVoltageLevel(node2, t2);
        if (voltageLevelId2 == null) {
            return;
        }

        addSwitchAdjacency(voltageLevelAdjacency, substationAdjacency, t1, t2, voltageLevelId1, voltageLevelId2);
    }

    private String nodeGetVoltageLevel(String node, CgmesTerminal t) {
        String voltageLevelId = null;
        if (node != null && !context.boundary().containsNode(node)) {
            voltageLevelId = context.cgmes().voltageLevel(t, context.nodeBreaker());
        }
        return voltageLevelId;
    }

    private void addSwitchAdjacency(Map<String, List<String>> voltageLevelAdjacency,
        Map<String, List<String>> substationAdjacency, CgmesTerminal t1, CgmesTerminal t2, String voltageLevelId1,
        String voltageLevelId2) {
        if (voltageLevelId1.equals(voltageLevelId2)) {
            return;
        }
        List<String> ad1 = voltageLevelAdjacency.get(voltageLevelId1);
        if (ad1 != null) {
            ad1.add(voltageLevelId2);
        }
        List<String> ad2 = voltageLevelAdjacency.get(voltageLevelId2);
        if (ad2 != null) {
            ad2.add(voltageLevelId1);
        }

        String substationId1 = context.cgmes().substation(t1, context.nodeBreaker());
        if (substationId1 == null) {
            return;
        }
        String substationId2 = context.cgmes().substation(t2, context.nodeBreaker());
        if (substationId2 == null) {
            return;
        }
        if (substationId1.equals(substationId2)) {
            return;
        }

        ad1 = substationAdjacency.get(substationId1);
        if (ad1 != null) {
            ad1.add(substationId2);
        }
        ad2 = substationAdjacency.get(substationId2);
        if (ad2 != null) {
            ad2.add(substationId1);
        }
    }

    private void addEnds(Map<String, List<String>> substationAdjacency, PropertyBags tends) {
        List<String> substationsIds = substationsIds(tends);
        if (substationsIds.size() <= 1) {
            return;
        }
        String sub0 = substationsIds.get(0);
        for (int i = 1; i < substationsIds.size(); i++) {
            String subi = substationsIds.get(i);
            if (sub0.contentEquals(subi)) {
                continue;
            }
            List<String> ad0 = substationAdjacency.get(sub0);
            if (ad0 != null) {
                ad0.add(subi);
            }
            List<String> adi = substationAdjacency.get(subi);
            if (adi != null) {
                adi.add(sub0);
            }
        }
    }

    private void buildVoltageLevel(Map<String, List<String>> voltageLevelAdjacency) {
        Set<String> visitedVoltageLevels = new HashSet<>();
        voltageLevelAdjacency.keySet().forEach(vl -> {
            if (!visitedVoltageLevels.contains(vl)) {
                ArrayList<String> vlAds = adjacents(voltageLevelAdjacency, visitedVoltageLevels, vl);
                String selectedVoltageLevelId = representativeVoltageLevelId(vlAds);
                for (String voltageLevelId : vlAds) {
                    if (!voltageLevelId.equals(selectedVoltageLevelId)) {
                        voltageLevelMapping.put(voltageLevelId, selectedVoltageLevelId);
                    }
                }
            }
        });
        if (!voltageLevelMapping.isEmpty()) {
            LOG.warn("VoltageLevel id mapping needed for {} voltageLevels: {}",
                voltageLevelMapping.size(), voltageLevelMapping);
        }
    }

    private void buildSubstation(Map<String, List<String>> substationAdjacency) {
        Set<String> visitedSubstations = new HashSet<>();
        substationAdjacency.keySet().forEach(sub -> {
            if (!visitedSubstations.contains(sub)) {
                ArrayList<String> subAds = adjacents(substationAdjacency, visitedSubstations, sub);

                String selectedSubstationId = representativeSubstationId(subAds);
                for (String substationId : subAds) {
                    if (!substationId.equals(selectedSubstationId)) {
                        substationMapping.put(substationId, selectedSubstationId);
                    }
                }
            }
        });
        if (!substationMapping.isEmpty()) {
            LOG.warn("Substation id mapping needed for {} substations: {}",
                    substationMapping.size(), substationMapping);
        }
    }

    private static ArrayList<String> adjacents(Map<String, List<String>> adjacency, Set<String> visited, String id) {
        ArrayList<String> adjacent = new ArrayList<>();
        adjacent.add(id);
        visited.add(id);

        int k = 0;
        while (k < adjacent.size()) {
            String vl0 = adjacent.get(k);
            if (adjacency.containsKey(vl0)) {
                adjacency.get(vl0).forEach(ad -> {
                    if (visited.contains(ad)) {
                        return;
                    }
                    adjacent.add(ad);
                    visited.add(ad);
                });
            }
            k++;
        }
        return adjacent;
    }

    private static String representativeVoltageLevelId(Collection<String> voltageLevelIds) {
        return voltageLevelIds.stream()
                .sorted()
                .findFirst()
                .orElse(voltageLevelIds.iterator().next());
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

    private List<String> substationsIds(PropertyBags tends) {
        List<String> substationsIds = new ArrayList<>();
        for (PropertyBag end : tends) {
            CgmesTerminal t = context.cgmes().terminal(end.getId(CgmesNames.TERMINAL));
            String node = context.nodeBreaker() ? t.connectivityNode() : t.topologicalNode();
            if (node != null && !context.boundary().containsNode(node)) {
                String sid = context.cgmes().substation(t, context.nodeBreaker());
                if (sid != null) {
                    substationsIds.add(context.namingStrategy().getId(CgmesNames.SUBSTATION, sid));
                }
            }
        }
        return substationsIds;
    }

    private final Context context;
    private final Map<String, String> substationMapping;
    private final Map<String, String> voltageLevelMapping;

    private static final Logger LOG = LoggerFactory.getLogger(SubstationIdMapping.class);
}
