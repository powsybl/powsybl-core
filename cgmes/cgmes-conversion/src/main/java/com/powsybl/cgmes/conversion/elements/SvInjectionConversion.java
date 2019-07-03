/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class SvInjectionConversion extends AbstractIdentifiedObjectConversion {

    public SvInjectionConversion(PropertyBag p, Context context) {
        super("SvInjection", p, context);
        String tn = p.getId("TopologicalNode");
        Terminal associatedTerminal = context.terminalMapping().findFromTopologicalNode(tn);
        if (associatedTerminal == null) {
            cgmesTerminal = context.cgmes().terminal(context.terminalMapping().findCgmesTerminalFromTopologicalNode(tn));
            voltageLevel = context.network().getVoltageLevel(context.cgmes().voltageLevel(cgmesTerminal));
            if (context.nodeBreaker()) {
                node = context.nodeMapping().iidmNodeForTerminal(cgmesTerminal, voltageLevel);
            }
        } else {
            voltageLevel = associatedTerminal.getVoltageLevel();
            if (context.nodeBreaker()) {
                VoltageLevel vl = associatedTerminal.getVoltageLevel();
                VoltageLevel.NodeBreakerView nb = vl.getNodeBreakerView();
                node = nb.getNodeCount();
                nb.setNodeCount(nb.getNodeCount() + 1);
                vl.getNodeBreakerView().newInternalConnection()
                        .setNode1(node)
                        .setNode2(associatedTerminal.getNodeBreakerView().getNode())
                        .add();
            }
        }
        if (!context.nodeBreaker()) {
            busId = context.namingStrategy().getId("Bus", tn);
        }
    }

    @Override
    public boolean valid() {
        if ((context.nodeBreaker() && node == -1) || (!context.nodeBreaker() && voltageLevel.getBusBreakerView().getBus(busId) == null)) {
            return false;
        }
        return voltageLevel != null;
    }

    @Override
    public void convert() {
        LoadAdder adder = voltageLevel.newLoad()
                .setP0(p.asDouble("pInjection"))
                .setQ0(q0())
                .setLoadType(LoadType.FICTITIOUS);
        identify(adder);
        if (context.nodeBreaker()) {
            adder.setNode(node);
        } else {
            adder.setConnectableBus(busId)
                .setBus(busId);
        }
        Load load = adder.add();
        if (cgmesTerminal != null) { // terminal only added if it has not been already added
            context.terminalMapping().add(cgmesTerminal.id(), load.getTerminal(), 1);
            if (cgmesTerminal.flow().defined()) {
                load.getTerminal().setP(cgmesTerminal.flow().p());
                load.getTerminal().setQ(cgmesTerminal.flow().q());
            }
        }
    }

    private double q0() {
        return p.containsKey("qInjection") ? p.asDouble("qInjection") : 0.0;
    }

    private VoltageLevel voltageLevel;
    private int node = -1;
    private String busId;
    private CgmesTerminal cgmesTerminal;
}
