package com.powsybl.cgmes;

/*
 * #%L
 * CGMES data model
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.powsybl.triplestore.PropertyBags;

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

    private Map<String, PropertyBags> computeGroupedTransformerEnds() {
        // TODO Instead of sorting after building each list, use a sorted collection when inserting
        Map<String, PropertyBags> gends = new HashMap<>();
        transformerEnds().stream()
                .forEach(end -> {
                    String id = end.getId("PowerTransformer");
                    PropertyBags ends = gends.computeIfAbsent(id, x -> new PropertyBags());
                    ends.add(end);
                });
        gends.entrySet().stream()
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
        terminals().stream().forEach(t -> {
            CgmesTerminal td = new CgmesTerminal(
                    t.getId("Terminal"),
                    t.getId("ConductingEquipment"),
                    t.getLocal("conductingEquipmentType"),
                    t.asBoolean("connected", false),
                    new PowerFlow(t, "p", "q"));
            ts.put(td.id(), td);
        });
        terminalsTP().stream().forEach(t -> {
            CgmesTerminal td = ts.get(t.getId("Terminal"));
            assert td != null;
            td.assignTP(t.getId("TopologicalNode"), t.getId("VoltageLevel"), t.getId("Substation"));
        });
        terminalsCN().stream().forEach(t -> {
            CgmesTerminal td = ts.get(t.getId("Terminal"));
            assert td != null;
            td.assignCN(t.getId("ConnectivityNode"), t.getId("TopologicalNode"), t.getId("VoltageLevel"),
                    t.getId("Substation"));
        });
        return ts;
    }

    private final Properties           properties;
    private Map<String, PropertyBags>  cachedGroupedTransformerEnds;
    private Map<String, CgmesTerminal> cachedTerminals;
}
