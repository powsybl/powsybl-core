/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.commons.PowsyblException;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CGMES standard: <br>
 * A PowerTransformer is contained in one Substation, but it can connect a Terminal to another different Substation <br>
 * A Switch can connect to different voltageLevels
 * <p>
 * IIDM Model: <br>
 * Ends of transformers need to be in the same substation<br>
 * Ends of switches need to be in the same voltageLevel
 * <p>
 * Solution: <br>
 * CGMES substations that are connected by transformers will be mapped to a single IIDM substation <br>
 * CGMES voltageLevels that are connected by switches will be mapped to a single IIDM voltageLevel
 * <p>
 * Example: <br>
 * We suppose that VL1, VL2, VL3, VL4, VL5, VL6 and VL7 are CGMES voltageLevels, <br>
 * Sw23 is a switch connecting voltageLevels VL2 and VL3, <br>
 * Sw34 is a switch connecting voltageLevels VL3 and VL4 and <br>
 * Sw67 is a switch connecting voltageLevels VL6 and VL7
 * <p>
 * Steps: <br>
 * Fill voltageLevelAdjacency Map <br>
 * Two voltageLevels are adjacent if they are connected by a switch <br>
 * The voltageLevelAdjacency Map will include the following records <br>
 * (VL1, []) <br>
 * (VL2, [VL2, VL3]) <br>
 * (VL3, [VL2, VL3, VL4]) <br>
 * (VL4, [VL3, VL4]) <br>
 * (VL5, []) <br>
 * (VL6, [VL6, VL7]) <br>
 * (VL7, [VL6, VL7]) <br>
 * <p>
 * For each non-visited VoltageLevel-key of the voltageLevelAdjacency Map all connected voltageLevels will be calculated  <br>
 * Two voltageLevels are connected if they are adjacent <br>
 * (allConnected method) <br>
 * All connected VoltageLevels to VL1 will be [VL1] <br>
 * All connected VoltageLevels to VL2 will be [VL2, VL3, VL4] <br>
 * All connected VoltageLevels to VL5 will be [VL5] <br>
 * All connected VoltageLevels to VL6 will be [VL6, VL7]
 * <p>
 * So the following voltageLevels should be merged <br>
 * [VL2, VL3, VL4] and the representative (IIDM voltageLevel) will be VL2 <br>
 * [VL6, VL7] and the representative (IIDM voltageLevel) will be VL6
 * <p>
 * And finally previous data is recorded in the voltageLevelMapping Map as <br>
 * (For each merged voltageLevel a record (merged voltageLevel, representative voltageLevel) is added) <br>
 * (VL3, VL2) <br>
 * (VL4, VL2) <br>
 * (VL7, VL6) <br>
 * <p>
 * The voltageLevelMapping Map will be used to assign the IIDM voltageLevel during the conversion process
 * <p>
 * The same algorithm is used to identify the substations that should be merged but: <br>
 * Two substations are adjacent if there is a transformer between them. <br>
 * The two substations associated with two adjacent voltageLevels, are adjacent if they are different substations.
 * <p>
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

public class SubstationIdMapping {

    public SubstationIdMapping(Context context) {
        this.context = context;
        this.substationMapping = new HashMap<>();
        this.voltageLevelMapping = new HashMap<>();
    }

    public boolean substationIsMapped(String cgmesIdentifier) {
        String sid = context.namingStrategy().getIidmId(CgmesNames.SUBSTATION, cgmesIdentifier);
        return substationMapping.containsKey(sid);
    }

    public String substationIidm(String cgmesIdentifier) {
        String sid = context.namingStrategy().getIidmId(CgmesNames.SUBSTATION, cgmesIdentifier);
        if (substationMapping.containsKey(sid)) {
            return substationMapping.get(sid);
        }
        return sid;
    }

    // All the keys for a given value, all the merged substations that have cgmesIdentifier as representative
    public Set<String> mergedSubstations(String cgmesIdentifier) {
        String sid = context.namingStrategy().getIidmId(CgmesNames.SUBSTATION, cgmesIdentifier);
        return substationMapping.entrySet().stream().filter(r -> r.getValue().equals(sid))
            .map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public boolean voltageLevelIsMapped(String cgmesIdentifier) {
        String vlid = context.namingStrategy().getIidmId(CgmesNames.VOLTAGE_LEVEL, cgmesIdentifier);
        return voltageLevelMapping.containsKey(vlid);
    }

    public String voltageLevelIidm(String cgmesIdentifier) {
        String vlid = context.namingStrategy().getIidmId(CgmesNames.VOLTAGE_LEVEL, cgmesIdentifier);
        if (voltageLevelMapping.containsKey(vlid)) {
            return voltageLevelMapping.get(vlid);
        }
        return vlid;
    }

    // All the keys for a given value, all the merged voltageLevels that have cgmesIdentifier as representative
    public Set<String> mergedVoltageLevels(String cgmesIdentifier) {
        String vlid = context.namingStrategy().getIidmId(CgmesNames.VOLTAGE_LEVEL, cgmesIdentifier);
        return voltageLevelMapping.entrySet().stream().filter(r -> r.getValue().equals(vlid))
            .map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public void build() {
        Map<String, Set<String>> voltageLevelAdjacency = new HashMap<>();
        Map<String, Set<String>> substationAdjacency = new HashMap<>();

        buildAdjacency(voltageLevelAdjacency, substationAdjacency);
        buildVoltageLevel(voltageLevelAdjacency);
        buildSubstation(substationAdjacency);
    }

    private void buildAdjacency(Map<String, Set<String>> voltageLevelAdjacency,
        Map<String, Set<String>> substationAdjacency) {
        context.cgmes().voltageLevels().forEach(vl -> addVoltageLevel(voltageLevelAdjacency, vl));
        context.cgmes().substations().forEach(st -> addSubstation(substationAdjacency, st));

        context.cgmes().switches().forEach(sw -> addSwitch(voltageLevelAdjacency, substationAdjacency, sw));
        context.cgmes().groupedTransformerEnds().forEach((t, tends) -> addEnds(substationAdjacency, tends));
    }

    private void addVoltageLevel(Map<String, Set<String>> voltageLevelAdjacency, PropertyBag vl) {
        String voltageLevelId = vl.getId(CgmesNames.VOLTAGE_LEVEL);
        String vId = context.namingStrategy().getIidmId(CgmesNames.VOLTAGE_LEVEL, voltageLevelId);
        voltageLevelAdjacency.put(vId, new HashSet<>());
    }

    private void addSubstation(Map<String, Set<String>> substationAdjacency, PropertyBag sub) {
        String substationlId = sub.getId(CgmesNames.SUBSTATION);
        String subId = context.namingStrategy().getIidmId(CgmesNames.SUBSTATION, substationlId);
        substationAdjacency.put(subId, new HashSet<>());
    }

    // Two different voltageLevels are adjacent if they are connected by a switch
    // If the corresponding substations are different they are also adjacent
    private void addSwitch(Map<String, Set<String>> voltageLevelAdjacency,
        Map<String, Set<String>> substationAdjacency, PropertyBag sw) {

        CgmesTerminal t1 = context.cgmes().terminal(sw.getId(CgmesNames.TERMINAL + 1));
        CgmesTerminal t2 = context.cgmes().terminal(sw.getId(CgmesNames.TERMINAL + 2));

        String voltageLevelId1 = context.cgmes().voltageLevel(t1, context.nodeBreaker());
        String voltageLevelId2 = context.cgmes().voltageLevel(t2, context.nodeBreaker());
        // Null could be received as voltageLevel at the boundary
        if (voltageLevelId1 == null || voltageLevelId2 == null || voltageLevelId1.equals(voltageLevelId2)) {
            return;
        }
        addAdjacency(voltageLevelAdjacency, voltageLevelId1, voltageLevelId2);

        String substationId1 = context.cgmes().substation(t1, context.nodeBreaker());
        String substationId2 = context.cgmes().substation(t2, context.nodeBreaker());
        // Null could be received as substation at the boundary
        if (substationId1 == null || substationId2 == null || substationId1.equals(substationId2)) {
            return;
        }
        addAdjacency(substationAdjacency, substationId1, substationId2);
    }

    // Two different substations are adjacent if they are connected by a transformer
    private void addEnds(Map<String, Set<String>> substationAdjacency, PropertyBags tends) {
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
            addAdjacency(substationAdjacency, sub0, subi);
        }
    }

    // Record in the adjacency Map that "id1 is adjacent to id2" and "id2 is adjacent to id1"
    private static void addAdjacency(Map<String, Set<String>> adjacency, String id1, String id2) {
        Set<String> ad1 = adjacency.get(id1);
        if (ad1 == null) {
            throw new PowsyblException("Unexpected reference to Substation or voltageLevel " + id1
                + ". It has not been defined in CGMES substations / voltageLevels.");
        }
        Set<String> ad2 = adjacency.get(id2);
        if (ad2 == null) {
            throw new PowsyblException("Unexpected reference to Substation or voltageLevel " + id2
                + ". It has not been defined in CGMES substations / voltageLevels.");
        }

        ad1.add(id2);
        ad2.add(id1);
    }

    private void buildVoltageLevel(Map<String, Set<String>> voltageLevelAdjacency) {
        Set<String> visitedVoltageLevels = new HashSet<>();
        for (String vl : voltageLevelAdjacency.keySet()) {
            if (!visitedVoltageLevels.contains(vl)) {
                Set<String> vlAds = allConnected(voltageLevelAdjacency, visitedVoltageLevels, vl);
                String selectedVoltageLevelId = representativeVoltageLevelId(vlAds);
                recordMergedIds(voltageLevelMapping, vlAds, selectedVoltageLevelId);
            }
        }
        if (!voltageLevelMapping.isEmpty()) {
            CgmesReports.voltageLevelMappingReport(context.getReportNode(), voltageLevelMapping.size(), voltageLevelMapping.toString());
            LOG.warn("Original {} VoltageLevel container(s) connected by switches have been merged in IIDM. Map of original VoltageLevel to IIDM: {}",
                voltageLevelMapping.size(), voltageLevelMapping);
        }
    }

    private void buildSubstation(Map<String, Set<String>> substationAdjacency) {
        Set<String> visitedSubstations = new HashSet<>();
        for (String sub : substationAdjacency.keySet()) {
            if (!visitedSubstations.contains(sub)) {
                Set<String> subAds = allConnected(substationAdjacency, visitedSubstations, sub);
                String selectedSubstationId = representativeSubstationId(subAds);
                recordMergedIds(substationMapping, subAds, selectedSubstationId);
            }
        }
        if (!substationMapping.isEmpty()) {
            CgmesReports.substationMappingReport(context.getReportNode(), substationMapping.size(), substationMapping.toString());
            LOG.warn("Original {} Substation container(s) connected by transformers have been merged in IIDM. Map of original Substation to IIDM: {}",
                substationMapping.size(), substationMapping);
        }
    }

    // Given an id (substation / voltageLevel) returns all connected ids (substations / voltageLevels)
    // Two ids are connected if they are adjacent in the adjacency Map
    private static Set<String> allConnected(Map<String, Set<String>> adjacency, Set<String> visited, String id) {
        ArrayList<String> allConnected = new ArrayList<>();

        // Insert id in the allConnected list and record it as visited
        allConnected.add(id);
        visited.add(id);

        // Expand, adding in each step all non-visited adjacent ids"
        int k = 0;
        while (k < allConnected.size()) {
            String vl0 = allConnected.get(k);
            if (adjacency.containsKey(vl0)) {
                adjacency.get(vl0).forEach(ad -> {
                    if (visited.contains(ad)) {
                        return;
                    }
                    allConnected.add(ad);
                    visited.add(ad);
                });
            }
            k++;
        }
        return new HashSet<>(allConnected);
    }

    private static String representativeVoltageLevelId(Collection<String> voltageLevelIds) {
        return voltageLevelIds.stream()
                .sorted()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unexpected: voltageLevelIds list is empty"));
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

    // For each merged substation a record (merged substation, representative substation) is added
    // For each merged voltageLevel a record (merged voltageLevel, representative voltageLevel) is added
    private static void recordMergedIds(Map<String, String> mapping, Collection<String> mergedIds,
        String representativeId) {
        for (String id : mergedIds) {
            if (!id.equals(representativeId)) {
                mapping.put(id, representativeId);
            }
        }
    }

    private List<String> substationsIds(PropertyBags tends) {
        List<String> substationsIds = new ArrayList<>();
        for (PropertyBag end : tends) {
            CgmesTerminal t = context.cgmes().terminal(end.getId(CgmesNames.TERMINAL));
            String node = context.nodeBreaker() ? t.connectivityNode() : t.topologicalNode();
            if (node != null && !context.boundary().containsNode(node)) {
                String sid = context.cgmes().substation(t, context.nodeBreaker());
                if (sid != null) {
                    substationsIds.add(context.namingStrategy().getIidmId(CgmesNames.SUBSTATION, sid));
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
