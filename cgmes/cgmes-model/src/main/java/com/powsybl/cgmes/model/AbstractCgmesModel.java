/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

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
            CgmesTerminal td = new CgmesTerminal(
                    t.getId(CgmesNames.TERMINAL),
                    t.getId("ConductingEquipment"),
                    t.getLocal("conductingEquipmentType"),
                    t.asBoolean("connected", false),
                    new PowerFlow(t, "p", "q"));
            ts.put(td.id(), td);
            conductingEquipmentTerminal.put(t.getId("ConductingEquipment"), t.getId(CgmesNames.TERMINAL));
        });
        terminalsTP().forEach(t -> {
            String tid = t.getId(CgmesNames.TERMINAL);
            CgmesTerminal td = ts.get(tid);
            if (td == null) {
                // Terminal from TopologicalNode is not found in the list of
                // Terminals linked to a ConductingEquipment
                String message = "The corresponding object of Terminal from TopologicalNode does not exist in the base model "
                        + tid;
                throw new CgmesModelException(message);
            }
            td.assignTP(t.getId("TopologicalNode"), t.getId("VoltageLevel"), t.getId("Substation"));
        });
        terminalsCN().forEach(t -> {
            String tid = t.getId(CgmesNames.TERMINAL);
            CgmesTerminal td = ts.get(tid);
            if (td == null) {
                // Terminal from ConnectivityNode is not found in the list of
                // Terminals linked to a ConductingEquipment
                String message = "The corresponding object of Terminal from ConnectivityNode does not exist in the base model "
                        + tid;
                throw new CgmesModelException(message);
            }
            td.assignCN(t.getId("ConnectivityNode"), t.getId("TopologicalNode"), t.getId("VoltageLevel"),
                    t.getId("Substation"));
        });
        return ts;
    }

    private final Properties properties;
    private Map<String, PropertyBags> cachedGroupedTransformerEnds;
    private Map<String, CgmesTerminal> cachedTerminals;
    private Map<String, String> conductingEquipmentTerminal;
    private Map<String, String> powerTransformerRatioTapChanger;
    private Map<String, String> powerTransformerPhaseTapChanger;
}
