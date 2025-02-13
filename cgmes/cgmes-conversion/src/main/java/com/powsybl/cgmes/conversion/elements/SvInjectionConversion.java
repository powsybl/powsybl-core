/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class SvInjectionConversion extends AbstractIdentifiedObjectConversion {

    public SvInjectionConversion(PropertyBag p, Context context) {
        super(CgmesNames.SV_INJECTION, p, context);
        String tn = p.getId("TopologicalNode");
        if (!findVoltageLevel(tn)) {
            return;
        }
        if (context.nodeBreaker()) {
            findNode(tn);
        } else {
            findBusId(tn);
        }
    }

    @Override
    public boolean valid() {
        return voltageLevel != null
                && (context.nodeBreaker() && node != -1
                    || !context.nodeBreaker() && voltageLevel.getBusBreakerView().getBus(busId) != null);
    }

    @Override
    public void convert() {
        double p0 = p.asDouble("pInjection");
        double q0 = p.asDouble("qInjection", 0.0);
        LoadAdder adder = voltageLevel.newLoad()
                .setP0(p0)
                .setQ0(q0)
                .setFictitious(true)
                .setLoadType(LoadType.FICTITIOUS);
        identify(adder);
        connect(adder);
        Load load = adder.add();
        if (cgmesTerminal != null) {
            PowerFlow f = cgmesTerminal.flow();
            if (!f.defined()) {
                f = new PowerFlow(p0, q0);
            }
            context.convertedTerminal(cgmesTerminal.id(), load.getTerminal(), 1, f);
        } else {
            load.getTerminal().setP(p0);
            load.getTerminal().setQ(q0);
        }

        addSpecificProperties(load);
    }

    private static void addSpecificProperties(Load load) {
        load.setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SV_INJECTION);
    }

    private void connect(LoadAdder adder) {
        if (context.nodeBreaker()) {
            adder.setNode(node);
        } else {
            adder.setConnectableBus(busId).setBus(busId);
        }
    }

    private boolean findVoltageLevel(String topologicalNode) {
        Terminal associatedTerminal = context.terminalMapping().findFromTopologicalNode(topologicalNode);
        if (associatedTerminal == null) {
            cgmesTerminal = context.cgmes().terminal(context.terminalMapping().findCgmesTerminalFromTopologicalNode(topologicalNode));
            if (cgmesTerminal == null || context.cgmes().voltageLevel(cgmesTerminal, context.nodeBreaker()) == null) {
                context.missing(id, () ->
                        String.format("The CGMES terminal and/or the voltage level associated to the topological node %s linked to the SV injection %s is missing",
                                topologicalNode, id));
                return false;
            }
            voltageLevel = context.network().getVoltageLevel(context.cgmes().voltageLevel(cgmesTerminal, context.nodeBreaker()));
        } else {
            voltageLevel = associatedTerminal.getVoltageLevel();
        }
        return true;
    }

    private void findBusId(String topologicalNode) {
        busId = context.namingStrategy().getIidmId("Bus", topologicalNode);
    }

    private void findNode(String topologicalNode) {
        Terminal associatedTerminal = context.terminalMapping().findFromTopologicalNode(topologicalNode);
        if (associatedTerminal == null) {
            findNodeFromUnmappedCgmesTerminal();
        } else {
            findNodeFromMappedCgmesTerminal(associatedTerminal, topologicalNode);
        }
    }

    private void findNodeFromUnmappedCgmesTerminal() {
        node = context.nodeMapping().iidmNodeForTerminal(cgmesTerminal, voltageLevel);
    }

    private void findNodeFromMappedCgmesTerminal(Terminal associatedTerminal, String topologicalNode) {
        node = context.nodeMapping().iidmNodeForTopologicalNode(topologicalNode, associatedTerminal.getNodeBreakerView().getNode(), associatedTerminal.getVoltageLevel());
    }

    private VoltageLevel voltageLevel;
    private int node = -1;
    private String busId;
    private CgmesTerminal cgmesTerminal;
}
