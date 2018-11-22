/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public abstract class AbstractCgmesModel implements CgmesModel {

    public AbstractCgmesModel() {
        this.properties = new Properties();
    }

    @Override
    public Properties getProperties() {
        return this.properties;
    }

    @Override
    public Map<String, PropertyBags> groupedTransformerEnds() {
        if (cachedGroupedTransformerEnds == null) {
            cachedGroupedTransformerEnds = computeGroupedTransformerEnds();
        }
        return cachedGroupedTransformerEnds;
    }

    @Override
    public CgmesTerminal terminal(String terminalId) {
        if (cachedTerminals == null) {
            cachedTerminals = computeTerminals();
        }
        return cachedTerminals.get(terminalId);
    }

    @Override
    public String terminalForEquipment(String conduntingEquipmentId) {
        // TODO Not all conducting equipment have a single terminal
        // For the current purposes of this mapping (export State Variables)
        // this is enough
        return conductingEquipmentTerminal.get(conduntingEquipmentId);
    }

    @Override
    public String ratioTapChangerForPowerTransformer(String powerTransformerId) {
        return powerTransformerRatioTapChanger.get(powerTransformerId);
    }

    @Override
    public String phaseTapChangerForPowerTransformer(String powerTransformerId) {
        return powerTransformerPhaseTapChanger.get(powerTransformerId);
    }

    @Override
    public PropertyBags connectivityNodes() {
        // FIXME(Luma) refactoring node-breaker conversion temporal
        if (cachedTerminals == null) {
            cachedTerminals = computeTerminals();
        }
        PropertyBags ns = new PropertyBags();
        Set<String> added = new HashSet<>();
        cachedTerminals.values().forEach(t -> {
            if (t.connectivityNode() != null && !added.contains(t.connectivityNode())) {
                added.add(t.connectivityNode());
                PropertyBag n = new PropertyBag(Arrays.asList("ConnectivityNode", "name", "ConnectivityNodeContainer", "TopologicalNode", "v", "angle"));
                n.put("ConnectivityNode", t.connectivityNode());
                n.put("name", t.connectivityNodeName());
                n.put("ConnectivityNodeContainer", t.connectivityNodeContainer());
                n.put("TopologicalNode", t.topologicalNode());
                n.put("v", t.v());
                n.put("angle", t.angle());
                ns.add(n);
            }
        });
        return ns;
    }

    @Override
    public PropertyBags topologicalNodes() {
        // FIXME(Luma) refactoring node-breaker conversion temporal
        if (cachedTerminals == null) {
            cachedTerminals = computeTerminals();
        }
        PropertyBags ns = new PropertyBags();
        Set<String> added = new HashSet<>();
        cachedTerminals.values().forEach(t -> {
            if (t.topologicalNode() != null && !added.contains(t.topologicalNode())) {
                added.add(t.topologicalNode());
                PropertyBag n = new PropertyBag(Arrays.asList("TopologicalNode", "name", "ConnectivityNodeContainer", "v", "angle"));
                n.put("TopologicalNode", t.topologicalNode());
                n.put("name", t.topologicalNodeName());
                n.put("ConnectivityNodeContainer", t.connectivityNodeContainerTopo());
                n.put("v", t.v());
                n.put("angle", t.angle());
                ns.add(n);
            }
        });
        return ns;
    }

    @Override
    public String substation(CgmesTerminal t) {
        CgmesContainer c = container(t);
        if (c == null) {
            return null;
        }
        return c.substation();
    }

    @Override
    public String voltageLevel(CgmesTerminal t) {
        CgmesContainer c = container(t);
        if (c == null) {
            return null;
        }
        return c.voltageLevel();
    }

    @Override
    public CgmesContainer container(String containerId) {
        if (cachedContainers == null) {
            cachedContainers = computeContainers();
        }
        return cachedContainers.get(containerId);
    }

    private CgmesContainer container(CgmesTerminal t) {
        String containerId = null;
        if (t.connectivityNodeContainer() != null) {
            containerId = t.connectivityNodeContainer();
        } else if (t.connectivityNodeContainerTopo() != null) {
            containerId = t.connectivityNodeContainerTopo();
        }
        if (containerId == null) {
            return null;
        }
        return container(containerId);
    }

    private Map<String, PropertyBags> computeGroupedTransformerEnds() {
        // Alternative implementation:
        // instead of sorting after building each list,
        // use a sorted collection when inserting
        Map<String, PropertyBags> gends = new HashMap<>();
        powerTransformerRatioTapChanger = new HashMap<>();
        powerTransformerPhaseTapChanger = new HashMap<>();
        transformerEnds()
                .forEach(end -> {
                    String id = end.getId("PowerTransformer");
                    PropertyBags ends = gends.computeIfAbsent(id, x -> new PropertyBags());
                    ends.add(end);
                    if (end.getId("PhaseTapChanger") != null) {
                        powerTransformerPhaseTapChanger.put(id, end.getId("PhaseTapChanger"));
                    } else if (end.getId("RatioTapChanger") != null) {
                        powerTransformerRatioTapChanger.put(id, end.getId("RatioTapChanger"));
                    }
                });
        gends.entrySet()
                .forEach(tends -> {
                    PropertyBags tends1 = new PropertyBags(
                            tends.getValue().stream()
                                    .sorted(Comparator
                                            .comparing(WindingType::fromTransformerEnd)
                                            .thenComparing(end -> end.asInt("endNumber", -1)))
                                    .collect(Collectors.toList()));
                    tends.setValue(tends1);
                });
        return gends;
    }

    private Map<String, CgmesTerminal> computeTerminals() {
        Map<String, CgmesTerminal> ts = new HashMap<>();
        conductingEquipmentTerminal = new HashMap<>();
        terminals().forEach(t -> {
            CgmesTerminal td = new CgmesTerminal(t);
            if (ts.containsKey(td.id())) {
                return;
            }
            ts.put(td.id(), td);
            conductingEquipmentTerminal.put(t.getId("ConductingEquipment"), t.getId(CgmesNames.TERMINAL));
        });
        return ts;
    }

    // FIXME(Luma): better caches create an object "Cache" that is final ...
    // (avoid filling all places with if cached == null...)
    private Map<String, CgmesContainer> computeContainers() {
        Map<String, CgmesContainer> cs = new HashMap<>();
        connectivityNodeContainers().forEach(c -> {
            String id = c.getId("ConnectivityNodeContainer");
            String voltageLevel = c.getId("VoltageLevel");
            String substation = c.getId("Substation");
            cs.put(id, new CgmesContainer(voltageLevel, substation));
        });
        return cs;
    }

    private final Properties properties;
    private Map<String, PropertyBags> cachedGroupedTransformerEnds;
    private Map<String, CgmesTerminal> cachedTerminals;
    private Map<String, CgmesContainer> cachedContainers;
    private Map<String, String> conductingEquipmentTerminal;
    private Map<String, String> powerTransformerRatioTapChanger;
    private Map<String, String> powerTransformerPhaseTapChanger;
}
