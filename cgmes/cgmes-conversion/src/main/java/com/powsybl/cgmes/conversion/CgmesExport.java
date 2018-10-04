package com.powsybl.cgmes.conversion;

import java.util.Arrays;
import java.util.List;

/*
 * #%L
 * CGMES conversion
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.util.Properties;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.CgmesModel;
import com.powsybl.cgmes.CgmesModelException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.PropertyBag;
import com.powsybl.triplestore.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {
    @Override
    public void export(Network network, Properties params, DataSource ds) {

        // Right now the network must contain the original CgmesModel
        // In the future it should be possible to export to CGMES directly from an IIDM Network
        CgmesModel cgmes = (CgmesModel) network.getProperties()
                .get(CgmesImport.NETWORK_PS_CGMES_MODEL);
        if (cgmes == null) {
            throw new CgmesModelException("No original CGMES model available in network");
        }

        // Fill the SV data of the CgmesModel with the network current state
        sv(network, cgmes);

        cgmes.write(ds);
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return "CGMES";
    }

    private void sv(Network n, CgmesModel cgmes) {
        PropertyBags voltages = new PropertyBags();
        for (Bus b : n.getBusBreakerView().getBuses()) {
            PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
            p.put("angle", fs(b.getAngle()));
            p.put("v", fs(b.getV()));
            p.put("TopologicalNode", topologicalNodeFromBusId(b.getId()));
            voltages.add(p);
        }
        cgmes.svVoltages(voltages);

        PropertyBags powerFlows = new PropertyBags();
        for (Load l : n.getLoads()) {
            PropertyBag p = new PropertyBag(SV_POWERFLOW_PROPERTIES);
            p.put("p", fs(l.getTerminal().getP()));
            p.put("q", fs(l.getTerminal().getQ()));
            p.put("Terminal", terminalFromTerminalId(l.getId()));
            powerFlows.add(p);
        }
        for (Generator g : n.getGenerators()) {
            PropertyBag p = new PropertyBag(SV_POWERFLOW_PROPERTIES);
            p.put("p", fs(g.getTerminal().getP()));
            p.put("q", fs(g.getTerminal().getQ()));
            p.put("Terminal", terminalFromTerminalId(g.getId()));
            powerFlows.add(p);
        }
        for (ShuntCompensator s : n.getShunts()) {
            PropertyBag p = new PropertyBag(SV_POWERFLOW_PROPERTIES);
            p.put("p", fs(s.getTerminal().getP()));
            p.put("q", fs(s.getTerminal().getQ()));
            p.put("Terminal", terminalFromTerminalId(s.getId()));
            powerFlows.add(p);
        }
        cgmes.svPowerFlows(powerFlows);

        PropertyBags shuntCompensatorSections = new PropertyBags();
        for (ShuntCompensator s : n.getShunts()) {
            PropertyBag p = new PropertyBag(SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES);
            p.put("continuousSections", is(s.getCurrentSectionCount()));
            p.put("ShuntCompensator", terminalFromTerminalId(s.getId()));
            shuntCompensatorSections.add(p);
        }
        cgmes.svShuntCompensatorSections(shuntCompensatorSections);

        PropertyBags tapSteps = new PropertyBags();
        for (TwoWindingsTransformer t : n.getTwoWindingsTransformers()) {
            PropertyBag p = new PropertyBag(SV_TAPSTEP_PROPERTIES);
            if (t.getPhaseTapChanger() != null) {
                p.put("continuousPosition", is(t.getPhaseTapChanger().getTapPosition()));
                p.put("TapChanger", terminalFromTerminalId(t.getId()));
                tapSteps.add(p);
            } else if (t.getRatioTapChanger() != null) {
                p.put("continuousPosition", is(t.getRatioTapChanger().getTapPosition()));
                p.put("TapChanger", terminalFromTerminalId(t.getId()));
                tapSteps.add(p);
            }
        }
        cgmes.svTapSteps(tapSteps);
    }

    private String fs(double value) {
        // TODO A double formatted as a valid CGMES string
        return "" + value;
    }

    private String is(int value) {
        // TODO A integer formatted as a valid CGMES string
        return "" + value;
    }

    private String topologicalNodeFromBusId(String iidmBusId) {
        // TODO Consider potential namingStrategy transformations
        return iidmBusId;
    }

    private String terminalFromTerminalId(String iidmTerminalId) {
        // TODO Consider potential namingStrategy transformations
        return iidmTerminalId;
    }

    private static final List<String> SV_VOLTAGE_PROPERTIES                  = Arrays.asList(
            "angle", "v", "TopologicalNode");

    private static final List<String> SV_POWERFLOW_PROPERTIES                = Arrays.asList(
            "p", "q", "Terminal");

    private static final List<String> SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES = Arrays.asList(
            "ShuntCompensator", "continuousSections");

    private static final List<String> SV_TAPSTEP_PROPERTIES                  = Arrays.asList(
            "continuousPosition", "TapChanger");
}
