/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.test.update;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStoreFactory;

public class CgmesModelUpdateTest {

    @Test
    public void test() throws IOException {
        ReadOnlyDataSource ds = gridModel.dataSource();
        List<String> implementations = TripleStoreFactory.onlyDefaultImplementation();
        assertFalse(implementations.isEmpty());

        String currentContext = "_SV";

        for (String impl : implementations) {

            CgmesModel cgmes = load(ds, impl);

            for (String context : cgmes.tripleStore().contextNames()) {
                if (context.toUpperCase().contains(currentContext.toUpperCase())
                    && !context.toUpperCase().contains("BD")
                    && !context.toUpperCase().contains("BOUNDARY")) {

                    cgmes.updateCgmes("updateCgmes", context, cgmes.getBaseName(), change());

                    PropertyBags actuals = cgmes.terminals();
                    PropertyBags expecteds = expected();
                    assertTrue(actuals.size() == expecteds.size());

                    Iterator<PropertyBag> a = actuals.iterator();
                    Iterator<PropertyBag> e = expecteds.iterator();
                    while (a.hasNext()) {
                        PropertyBag ap = a.next();
                        PropertyBag ep = e.next();

                        assertTrue(ap.entrySet().stream()
                            .allMatch(x -> x.getValue()
                                .equals(ep.get(x.getKey()))));
                    }
                }
            }
        }
    }

    private CgmesModel load(ReadOnlyDataSource ds, String impl) {
        CgmesModel cgmes = CgmesModelFactory.create(ds, impl);
        cgmes.print(LOG::info);
        return cgmes;
    }

    private Map<String, String> change() {
        Map<String, String> cgmesChanges = new HashMap<>();
        cgmesChanges.put("cgmesSubject", "_SVPF_0");
        cgmesChanges.put("cgmesPredicate", "cim:SvPowerFlow.p");
        cgmesChanges.put("cgmesNewValue", "39");
        cgmesChanges.put("valueIsNode", "false");
        return cgmesChanges;
    }

    private PropertyBags expected() {
        PropertyBags expected = new PropertyBags();
        // keys
        String graph = "graph";
        String graphTP = "graphTP";
        String graphSV = "graphSV";
        String graphTP2 = "graphTP2";
        String terminal = "Terminal";
        String conductingEquipment = "ConductingEquipment";
        String conductingEquipmentType = "conductingEquipmentType";
        String connected = "connected";
        String svPowerFlow = "SvPowerFlow";
        String p = "p";
        String q = "q";
        String topologicalNode = "TopologicalNode";
        // values
        String graphEQValue = "contexts:case1_EQ.xml";
        String graphTPValue = "contexts:case1_TP.xml";
        String graphSVValue = "contexts:case1_SV.xml";
        String connectedValue = "true";

        List<String> properties = Arrays.asList(terminal, graph, conductingEquipment,
            conductingEquipmentType, connected, graphTP, graphSV, svPowerFlow, p, q,
            topologicalNode, graphTP2);

        PropertyBag p0 = new PropertyBag(properties);
        PropertyBag p1 = new PropertyBag(properties);
        PropertyBag p2 = new PropertyBag(properties);
        PropertyBag p3 = new PropertyBag(properties);
        PropertyBag p4 = new PropertyBag(properties);
        PropertyBag p5 = new PropertyBag(properties);
        expected.add(p0);
        expected.add(p1);
        expected.add(p2);
        expected.add(p3);
        expected.add(p4);
        expected.add(p5);
        p0.put(graph, graphEQValue);
        p1.put(graph, graphEQValue);
        p2.put(graph, graphEQValue);
        p3.put(graph, graphEQValue);
        p4.put(graph, graphEQValue);
        p5.put(graph, graphEQValue);
        p0.put(terminal, "http://case1/#_GEN_____-GRID____-1_TW_EX_TE");
        p1.put(terminal, "http://case1/#_GEN_____-GRID____-1_TW_OR_TE");
        p2.put(terminal, "http://case1/#_GEN______SM_TE");
        p3.put(terminal, "http://case1/#_GRID____-INF_____-1_AC_TE_EX");
        p4.put(terminal, "http://case1/#_GRID____-INF_____-1_AC_TE_OR");
        p5.put(terminal, "http://case1/#_INF______SM_TE");
        p0.put(conductingEquipment, "http://case1/#_GEN_____-GRID____-1_TW_EX");
        p1.put(conductingEquipment, "http://case1/#_GEN_____-GRID____-1_TW_OR");
        p2.put(conductingEquipment, "http://case1/#_GEN______SM");
        p3.put(conductingEquipment, "http://case1/#_GRID____-INF_____-1_AC");
        p4.put(conductingEquipment, "http://case1/#_GRID____-INF_____-1_AC");
        p5.put(conductingEquipment, "http://case1/#_INF______SM");
        p0.put(conductingEquipmentType, "http://iec.ch/TC57/2009/CIM-schema-cim14#TransformerWinding");
        p1.put(conductingEquipmentType, "http://iec.ch/TC57/2009/CIM-schema-cim14#TransformerWinding");
        p2.put(conductingEquipmentType, "http://iec.ch/TC57/2009/CIM-schema-cim14#SynchronousMachine");
        p3.put(conductingEquipmentType, "http://iec.ch/TC57/2009/CIM-schema-cim14#ACLineSegment");
        p4.put(conductingEquipmentType, "http://iec.ch/TC57/2009/CIM-schema-cim14#ACLineSegment");
        p5.put(conductingEquipmentType, "http://iec.ch/TC57/2009/CIM-schema-cim14#SynchronousMachine");
        p0.put(connected, connectedValue);
        p1.put(connected, connectedValue);
        p2.put(connected, connectedValue);
        p3.put(connected, connectedValue);
        p4.put(connected, connectedValue);
        p5.put(connected, connectedValue);
        p0.put(graphTP, graphTPValue);
        p1.put(graphTP, graphTPValue);
        p2.put(graphTP, graphTPValue);
        p3.put(graphTP, graphTPValue);
        p4.put(graphTP, graphTPValue);
        p5.put(graphTP, graphTPValue);
        p2.put(graphSV, graphSVValue);
        p5.put(graphSV, graphSVValue);
        p2.put(svPowerFlow, "http://case1/#_SVPF_0");
        p5.put(svPowerFlow, "http://case1/#_SVPF_1");
        p2.put(p, "39");
        p5.put(p, "-0");
        p2.put(q, "-0");
        p5.put(q, "-0");
        p0.put(topologicalNode, "http://case1/#_GRID_____TN");
        p1.put(topologicalNode, "http://case1/#_GEN______TN");
        p2.put(topologicalNode, "http://case1/#_GEN______TN");
        p3.put(topologicalNode, "http://case1/#_INF______TN");
        p4.put(topologicalNode, "http://case1/#_GRID_____TN");
        p5.put(topologicalNode, "http://case1/#_INF______TN");
        p0.put(graphTP2, graphTPValue);
        p1.put(graphTP2, graphTPValue);
        p2.put(graphTP2, graphTPValue);
        p3.put(graphTP2, graphTPValue);
        p4.put(graphTP2, graphTPValue);
        p5.put(graphTP2, graphTPValue);

        return expected;
    }

    private final TestGridModel gridModel = Cim14SmallCasesCatalog.small1();

    private static final Logger LOG = LoggerFactory.getLogger(CgmesModelUpdateTest.class);
}
