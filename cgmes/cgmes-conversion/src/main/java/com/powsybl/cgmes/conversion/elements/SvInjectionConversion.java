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
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class SvInjectionConversion extends AbstractIdentifiedObjectConversion {

    public SvInjectionConversion(PropertyBag p, Context context) {
        super(CgmesNames.SV_INJECTION, p, context);
        String tn = p.getId("TopologicalNode");
        if (context.nodeBreaker()) {
            String connectivityNode = p.getId("ConnectivityNode");
            if (connectivityNode != null) {
                findNode(connectivityNode);
                findVoltageLevelFromConnectivityNode(connectivityNode);
            }
        } else {
            findBusId(tn);
            findVoltageLevelFromBusId(busId);
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
        load.getTerminal().setP(p0);
        load.getTerminal().setQ(q0);

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

    private void findBusId(String topologicalNode) {
        busId = context.namingStrategy().getIidmId("Bus", topologicalNode);
    }

    private void findVoltageLevelFromBusId(String busId) {
        Bus bus = context.network().getBusBreakerView().getBus(busId);
        if (bus != null) {
            voltageLevel = bus.getVoltageLevel();
        }
    }

    private void findNode(String connectivityNode) {
        context.nodeMapping().getIidmNodeForConnectivityNode(connectivityNode).ifPresent(n -> node = n);
    }

    private void findVoltageLevelFromConnectivityNode(String connectivityNode) {
        context.nodeMapping().getVoltageLevelIdForConnectivityNode(connectivityNode).ifPresent(voltageLevelId -> voltageLevel = context.network().getVoltageLevel(voltageLevelId));
    }

    private VoltageLevel voltageLevel;
    private int node = -1;
    private String busId;
}
