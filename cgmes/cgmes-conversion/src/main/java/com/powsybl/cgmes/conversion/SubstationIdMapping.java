/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesContainer;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.apache.commons.lang3.tuple.Pair;
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
        this.fictitiousVoltageLevels = new HashMap<>();
        this.referenceVoltageLevels = new HashMap<>();
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
        Map<String, Set<String>> fictitiousVoltageLevelInterAdjacency = new HashMap<>();
        Map<String, Set<String>> fictitiousVoltageLevelIntraAdjacency = new HashMap<>();

        buildAdjacency(voltageLevelAdjacency, substationAdjacency, fictitiousVoltageLevelInterAdjacency, fictitiousVoltageLevelIntraAdjacency);
        buildVoltageLevel(voltageLevelAdjacency);
        buildSubstation(substationAdjacency);
        buildReferencesVoltageLevels(fictitiousVoltageLevelInterAdjacency, fictitiousVoltageLevelIntraAdjacency);
    }

    private void buildAdjacency(Map<String, Set<String>> voltageLevelAdjacency, Map<String, Set<String>> substationAdjacency,
                                Map<String, Set<String>> fictitiousVoltageLevelInterAdjacency,
                                Map<String, Set<String>> fictitiousVoltageLevelIntraAdjacency) {
        boolean fictitiousVoltageLevelForEveryNode = context.config().getCreateFictitiousVoltageLevelsForEveryNode();

        context.cgmes().switches().forEach(sw -> addSwitch(voltageLevelAdjacency, substationAdjacency, sw, fictitiousVoltageLevelForEveryNode));
        context.cgmes().groupedTransformerEnds().forEach((t, tends) -> addEnds(substationAdjacency, tends));

        context.cgmes().acLineSegments().forEach(ac -> addReference(fictitiousVoltageLevelInterAdjacency, fictitiousVoltageLevelIntraAdjacency, ac, fictitiousVoltageLevelForEveryNode));
        context.cgmes().seriesCompensators().forEach(sc -> addReference(fictitiousVoltageLevelInterAdjacency, fictitiousVoltageLevelIntraAdjacency, sc, fictitiousVoltageLevelForEveryNode));
    }

    // Two different voltageLevels are adjacent if they are connected by a switch
    // If the corresponding substations are different they are also adjacent
    private void addSwitch(Map<String, Set<String>> voltageLevelAdjacency,
                           Map<String, Set<String>> substationAdjacency,
                           PropertyBag sw, boolean fictitiousVoltageLevelForEveryNode) {

        CgmesTerminal t1 = context.cgmes().terminal(sw.getId(CgmesNames.TERMINAL + 1));
        CgmesTerminal t2 = context.cgmes().terminal(sw.getId(CgmesNames.TERMINAL + 2));

        Optional<String> nodeId1 = context.cgmes().node(t1, context.nodeBreaker());
        Optional<String> nodeId2 = context.cgmes().node(t2, context.nodeBreaker());
        if (nodeId1.isPresent() && !context.boundary().containsNode(nodeId1.get()) && nodeId2.isPresent() && !context.boundary().containsNode(nodeId2.get())) {
            Optional<CgmesContainer> cgmesContainer1 = context.cgmes().nodeContainer(nodeId1.get());
            Optional<CgmesContainer> cgmesContainer2 = context.cgmes().nodeContainer(nodeId2.get());

            if (cgmesContainer1.isPresent() && cgmesContainer2.isPresent()) {
                String voltageLevelId1 = obtainVoltageLevelAndRecordItIfItIsFictitious(cgmesContainer1.get(), nodeId1.get(), fictitiousVoltageLevelForEveryNode);
                String voltageLevelId2 = obtainVoltageLevelAndRecordItIfItIsFictitious(cgmesContainer2.get(), nodeId2.get(), fictitiousVoltageLevelForEveryNode);

                addAdjacency(voltageLevelAdjacency, voltageLevelId1, voltageLevelId2);
                addAdjacency(substationAdjacency, cgmesContainer1.get().substation(), cgmesContainer2.get().substation());
            }
        }
    }

    private String obtainVoltageLevelAndRecordItIfItIsFictitious(CgmesContainer cgmesContainer, String nodeId, boolean fictitiousVoltageLevelForEveryNode) {
        if (cgmesContainer.isVoltageLevel()) {
            return cgmesContainer.voltageLevel();
        } else {
            String fictitiousVoltageLevelId = Conversion.getFictitiousVoltageLevelForContainer(cgmesContainer.id(), nodeId, fictitiousVoltageLevelForEveryNode);
            recordFictitiousVoltageLevel(fictitiousVoltageLevels, fictitiousVoltageLevelId, cgmesContainer.id(), nodeId);
            return fictitiousVoltageLevelId;
        }
    }

    private static void recordFictitiousVoltageLevel(Map<String, Pair<String, Set<String>>> fictitiousVoltageLevels, String fictitiousVoltageLevelId, String cgmesContainerId, String nodeId) {
        if (fictitiousVoltageLevels.containsKey(fictitiousVoltageLevelId)) {
            if (fictitiousVoltageLevels.get(fictitiousVoltageLevelId).getLeft().equals(cgmesContainerId)) {
                fictitiousVoltageLevels.get(fictitiousVoltageLevelId).getRight().add(nodeId);
            } else {
                throw new ConversionException("Unexpected cgmesContainerId: " + cgmesContainerId);
            }
        } else {
            Set<String> nodeIdSet = new HashSet<>();
            nodeIdSet.add(nodeId);
            fictitiousVoltageLevels.put(fictitiousVoltageLevelId, Pair.of(cgmesContainerId, nodeIdSet));
        }
    }

    // Two different substations are adjacent if they are connected by a transformer
    private void addEnds(Map<String, Set<String>> substationAdjacency, PropertyBags tends) {
        List<String> substationsIds = substationsIds(tends);
        if (substationsIds.size() <= 1) {
            return;
        }
        String sub0 = substationsIds.get(0);
        for (int i = 1; i < substationsIds.size(); i++) {
            addAdjacency(substationAdjacency, sub0, substationsIds.get(i));
        }
    }

    private void addReference(Map<String, Set<String>> fictitiousVoltageLevelInterAdjacency,
                              Map<String, Set<String>> fictitiousVoltageLevelIntraAdjacency,
                              PropertyBag equipment, boolean fictitiousVoltageLevelByNode) {

        CgmesTerminal t1 = context.cgmes().terminal(equipment.getId(CgmesNames.TERMINAL + 1));
        CgmesTerminal t2 = context.cgmes().terminal(equipment.getId(CgmesNames.TERMINAL + 2));

        Optional<String> nodeId1 = context.cgmes().node(t1, context.nodeBreaker());
        Optional<String> nodeId2 = context.cgmes().node(t2, context.nodeBreaker());
        if (nodeId1.isPresent() && !context.boundary().containsNode(nodeId1.get()) && nodeId2.isPresent() && !context.boundary().containsNode(nodeId2.get())) {
            Optional<CgmesContainer> cgmesContainer1 = context.cgmes().nodeContainer(nodeId1.get());
            Optional<CgmesContainer> cgmesContainer2 = context.cgmes().nodeContainer(nodeId2.get());

            if (cgmesContainer1.isPresent() && cgmesContainer2.isPresent()) {
                if (isInterAdjacency(cgmesContainer1.get(), cgmesContainer2.get())) {

                    String voltageLevelId1 = obtainVoltageLevelAndRecordItIfItIsFictitious(cgmesContainer1.get(), nodeId1.get(), fictitiousVoltageLevelByNode);
                    String voltageLevelId2 = obtainVoltageLevelAndRecordItIfItIsFictitious(cgmesContainer2.get(), nodeId2.get(), fictitiousVoltageLevelByNode);
                    addAdjacency(fictitiousVoltageLevelInterAdjacency, voltageLevelId1, voltageLevelId2);
                } else if (isIntraAdjacency(cgmesContainer1.get(), cgmesContainer2.get())) {

                    String voltageLevelId1 = obtainVoltageLevelAndRecordItIfItIsFictitious(cgmesContainer1.get(), nodeId1.get(), fictitiousVoltageLevelByNode);
                    String voltageLevelId2 = obtainVoltageLevelAndRecordItIfItIsFictitious(cgmesContainer2.get(), nodeId2.get(), fictitiousVoltageLevelByNode);
                    addAdjacency(fictitiousVoltageLevelIntraAdjacency, voltageLevelId1, voltageLevelId2);
                }
            }
        }
    }

    private static boolean isInterAdjacency(CgmesContainer cgmesContainer1, CgmesContainer cgmesContainer2) {
        return cgmesContainer1.isVoltageLevel() && !cgmesContainer2.isVoltageLevel() || !cgmesContainer1.isVoltageLevel() && cgmesContainer2.isVoltageLevel();
    }

    private static boolean isIntraAdjacency(CgmesContainer cgmesContainer1, CgmesContainer cgmesContainer2) {
        return !cgmesContainer1.isVoltageLevel() && !cgmesContainer2.isVoltageLevel();
    }

    private static void addAdjacency(Map<String, Set<String>> adjacency, String id1, String id2) {
        if (!isValidAdjacency(id1, id2)) {
            return;
        }
        adjacency.computeIfAbsent(id1, k -> new HashSet<>()).add(id2);
        adjacency.computeIfAbsent(id2, k -> new HashSet<>()).add(id1);
    }

    private static boolean isValidAdjacency(String ad1, String ad2) {
        return ad1 != null && ad2 != null && !ad1.equals(ad2);
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
            LOG.warn("Original {} Substation container(s) connected by transformers have been merged in IIDM. Map of original Substation to IIDM: {}",
                substationMapping.size(), substationMapping);
        }
    }

    private void buildReferencesVoltageLevels(Map<String, Set<String>> fictitiousVoltageLevelInterAdjacency, Map<String, Set<String>> fictitiousVoltageLevelIntraAdjacency) {

        voltageLevelMapping.forEach((key, value) -> {
            if (fictitiousVoltageLevels.containsKey(key) && !fictitiousVoltageLevels.containsKey(value)) {
                referenceVoltageLevels.put(key, value);
            }
        });

        Set<String> visitedInter = new HashSet<>();
        for (String fictitiousVoltageLevel : fictitiousVoltageLevelInterAdjacency.keySet()) {
            if (!visitedInter.contains(fictitiousVoltageLevel)) {
                Set<String>vlAds = allConnected(fictitiousVoltageLevelInterAdjacency, visitedInter, fictitiousVoltageLevel);
                referenceVoltageLevel(vlAds).ifPresent(referenceId -> recordReferenceVoltageLevel(vlAds, referenceId));
            }
        }

        Set<String> visitedIntra = new HashSet<>();
        for (String fictitiousVoltageLevel : fictitiousVoltageLevelIntraAdjacency.keySet()) {
            if (!visitedIntra.contains(fictitiousVoltageLevel)) {
                Set<String>vlAds = allConnected(fictitiousVoltageLevelIntraAdjacency, visitedIntra, fictitiousVoltageLevel);
                referenceVoltageLevel(vlAds).ifPresent(referenceId -> recordReferenceVoltageLevel(vlAds, referenceId));
            }
        }

        // All fictitious voltage levels need a reference voltage level
        Optional<String> fictitiousVoltageLevelWithoutReference = fictitiousVoltageLevels.keySet().stream()
                .filter(fictitiousVoltageLevel -> !referenceVoltageLevels.containsKey(fictitiousVoltageLevel))
                .findAny();
        if (fictitiousVoltageLevelWithoutReference.isPresent()) {
            throw new ConversionException("Fictitious voltage level without reference: " + fictitiousVoltageLevelWithoutReference.get());
        }
    }

    private Optional<String> referenceVoltageLevel(Set<String> voltageLevelIds) {
        Optional<String> referenceId = voltageLevelIds.stream()
                .filter(referenceVoltageLevels::containsKey)
                .map(referenceVoltageLevels::get)
                .min(Comparator.naturalOrder());
        if (referenceId.isPresent()) {
            return referenceId;
        }
        return voltageLevelIds.stream()
                .filter(vl -> !fictitiousVoltageLevels.containsKey(vl))
                .min(Comparator.naturalOrder());
    }

    private void recordReferenceVoltageLevel(Set<String> voltageLevelIds, String referenceId) {
        voltageLevelIds.stream()
                .filter(vl -> fictitiousVoltageLevels.containsKey(vl) && !referenceVoltageLevels.containsKey(vl))
                .forEach(vl -> referenceVoltageLevels.put(vl, referenceId));
    }

    // Given an id (substation / voltageLevel) returns all connected ids (substations / voltageLevels)
    // Two ids are connected if they are adjacent in the adjacency Map
    private static Set<String> allConnected(Map<String, Set<String>> adjacency, Set<String> visited, String id) {
        ArrayList<String> allConnected = new ArrayList<>();

        // Insert id in the allConnected list and record it as visited
        allConnected.add(id);
        visited.add(id);

        // Expand, adding in each step all non-visited adjacent ids
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

    private String representativeVoltageLevelId(Collection<String> voltageLevelIds) {
        Optional<String> voltageLevelContainerId = voltageLevelIds.stream()
                .filter(voltageLevelId -> !fictitiousVoltageLevels.containsKey(voltageLevelId))
                .sorted()
                .findFirst();
        return voltageLevelContainerId.orElseGet(() -> voltageLevelIds.stream()
                .sorted()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unexpected: voltageLevelIds list is empty")));
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

            Optional<String> nodeId = context.cgmes().node(t, context.nodeBreaker());
            if (nodeId.isPresent() && !context.boundary().containsNode(nodeId.get())) {
                Optional<CgmesContainer> cgmesContainer = context.cgmes().nodeContainer(nodeId.get());

                if (cgmesContainer.isPresent()) {
                    String sid = cgmesContainer.get().substation();
                    if (sid != null) {
                        substationsIds.add(context.namingStrategy().getIidmId(CgmesNames.SUBSTATION, sid));
                    }
                }
            }
        }
        return substationsIds;
    }

    // Only iidm voltage levels that are fictitious
    Set<String> getFictitiousVoltageLevelsToBeCreated() {
        return fictitiousVoltageLevels.keySet().stream()
                .map(this::voltageLevelIidm)
                .filter(fictitiousVoltageLevels::containsKey)
                .collect(Collectors.toSet());
    }

    Optional<String> getContainerId(String fictitiousVoltageLevelId) {
        return fictitiousVoltageLevels.containsKey(fictitiousVoltageLevelId) ? Optional.of(fictitiousVoltageLevels.get(fictitiousVoltageLevelId).getLeft()) : Optional.empty();
    }

    Optional<String> getContainerName(String fictitiousVoltageLevelId) {
        Optional<String> containerId = getContainerId(fictitiousVoltageLevelId);
        if (containerId.isPresent()) {
            CgmesContainer cgmesContainer = context.cgmes().container(containerId.get());
            return cgmesContainer != null ? Optional.of(cgmesContainer.name()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    boolean isSubstationContainer(String fictitiousVoltageLevelId) {
        Optional<String> containerId = getContainerId(fictitiousVoltageLevelId);
        if (containerId.isPresent()) {
            CgmesContainer cgmesContainer = context.cgmes().container(containerId.get());
            return cgmesContainer != null && cgmesContainer.isSubstation();
        } else {
            return false;
        }
    }

    Optional<String> getReferenceVoltageLevelId(String fictitiousVoltageLevelId) {
        if (referenceVoltageLevels.containsKey(fictitiousVoltageLevelId)) {
            String referenceVoltageLevelId = referenceVoltageLevels.get(fictitiousVoltageLevelId);
            return Optional.ofNullable(voltageLevelIidm(referenceVoltageLevelId));
        } else {
            return Optional.empty();
        }
    }

    private final Context context;
    private final Map<String, String> substationMapping;
    private final Map<String, String> voltageLevelMapping;
    private final Map<String, Pair<String, Set<String>>> fictitiousVoltageLevels;
    private final Map<String, String> referenceVoltageLevels;

    private static final Logger LOG = LoggerFactory.getLogger(SubstationIdMapping.class);
}
