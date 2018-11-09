/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.Arrays;
import java.util.List;

import java.util.Properties;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.Subset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {
    @Override
    public void export(Network network, Properties params, DataSource ds) {

        // Right now the network must contain the original CgmesModel
        // In the future it should be possible to export to CGMES
        // directly from an IIDM Network
        CgmesModel cgmes = (CgmesModel) network.getProperties()
                .get(CgmesImport.NETWORK_PS_CGMES_MODEL);
        if (cgmes == null) {
            throw new CgmesModelException("No original CGMES model available in network");
        }

        // Clear the previous SV data
        cgmes.clear(Subset.STATE_VARIABLES);

        // Fill the SV data of the CgmesModel with the network current state
        addStateVariables(network, cgmes);

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

    private void addStateVariables(Network n, CgmesModel cgmes) {
        String contextName = "SV";

        // TODO Add full model data with proper profile (StateVariables)

        PropertyBags voltages = new PropertyBags();
        for (Bus b : n.getBusBreakerView().getBuses()) {
            PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
            p.put("angle", fs(b.getAngle()));
            p.put("v", fs(b.getV()));
            p.put("TopologicalNode", topologicalNodeFromBusId(b.getId()));
            voltages.add(p);
        }
        cgmes.add(contextName, "SvVoltage", voltages);

        PropertyBags powerFlows = new PropertyBags();
        for (Load l : n.getLoads()) {
            powerFlows.add(createPowerFlowProperties(cgmes, l.getTerminal()));
        }
        for (Generator g : n.getGenerators()) {
            powerFlows.add(createPowerFlowProperties(cgmes, g.getTerminal()));
        }
        for (ShuntCompensator s : n.getShuntCompensators()) {
            powerFlows.add(createPowerFlowProperties(cgmes, s.getTerminal()));
        }
        cgmes.add(contextName, "SvPowerFlow", powerFlows);

        PropertyBags shuntCompensatorSections = new PropertyBags();
        for (ShuntCompensator s : n.getShuntCompensators()) {
            PropertyBag p = new PropertyBag(SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES);
            p.put("continuousSections", is(s.getCurrentSectionCount()));
            p.put("ShuntCompensator", s.getId());
            shuntCompensatorSections.add(p);
        }
        cgmes.add(contextName, "SvShuntCompensatorSections", shuntCompensatorSections);

        PropertyBags tapSteps = new PropertyBags();
        for (TwoWindingsTransformer t : n.getTwoWindingsTransformers()) {
            PropertyBag p = new PropertyBag(SV_TAPSTEP_PROPERTIES);
            // TODO If we could store an identifier for the tap changer in IIDM
            // then we would not need to query the CGMES model
            if (t.getPhaseTapChanger() != null) {
                p.put(CgmesNames.POSITION, is(t.getPhaseTapChanger().getTapPosition()));
                p.put(CgmesNames.TAP_CHANGER, cgmes.phaseTapChangerForPowerTransformer(t.getId()));
                tapSteps.add(p);
            } else if (t.getRatioTapChanger() != null) {
                p.put(CgmesNames.POSITION, is(t.getRatioTapChanger().getTapPosition()));
                p.put(CgmesNames.TAP_CHANGER, cgmes.ratioTapChangerForPowerTransformer(t.getId()));
                tapSteps.add(p);
            }
        }
        cgmes.add(contextName, "SvTapStep", tapSteps);
    }

    private PropertyBag createPowerFlowProperties(CgmesModel cgmes, Terminal terminal) {
        PropertyBag p = new PropertyBag(SV_POWERFLOW_PROPERTIES);
        p.put("p", fs(terminal.getP()));
        p.put("q", fs(terminal.getQ()));
        // TODO If we could store a terminal identifier in IIDM
        // we would not need to obtain it querying CGMES for the related equipment
        p.put(CgmesNames.TERMINAL, cgmes.terminalForEquipment(terminal.getConnectable().getId()));
        return p;
    }

    private String fs(double value) {
        return "" + value;
    }

    private String is(int value) {
        return "" + value;
    }

    private String topologicalNodeFromBusId(String iidmBusId) {
        // TODO Consider potential namingStrategy transformations
        return iidmBusId;
    }

    private static final List<String> SV_VOLTAGE_PROPERTIES = Arrays.asList("angle", "v", "TopologicalNode");
    private static final List<String> SV_POWERFLOW_PROPERTIES = Arrays.asList("p", "q", CgmesNames.TERMINAL);
    private static final List<String> SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES = Arrays.asList("ShuntCompensator",
            "continuousSections");
    private static final List<String> SV_TAPSTEP_PROPERTIES = Arrays.asList(CgmesNames.POSITION,
            CgmesNames.TAP_CHANGER);
}
