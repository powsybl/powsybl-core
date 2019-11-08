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
import com.powsybl.cgmes.conversion.update.CgmesUpdate;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {

    public CgmesExport() {
        this.profiling = new Profiling();
    }

    public CgmesExport(Profiling profiling) {
        this.profiling = profiling;
    }

    @Override
    public void export(Network network, Properties params, DataSource ds) {

        // Right now the network must contain the original CgmesModel
        // In the future it should be possible to export to CGMES
        // directly from an IIDM Network
        CgmesModelExtension ext = network.getExtension(CgmesModelExtension.class);
        if (ext == null) {
            throw new CgmesModelException("No extension for CGMES model found in Network");
        }
        // TODO elena
        // When export is called, it triggers:
        // Apply network changes to cgmes:
        // Create clone repo from the origin; update the clone with changes list;
        // Refresh/rebuild caches --> done in clone code
        // Clear the previous SV data - we can keep addStateVariables(network, cgmes)
        // here, or distribut between the appropriate elements.
        CgmesModel cgmesSource = ext.getCgmesModel();
        profiling.start();
        CgmesModel cgmes = CgmesModelFactory.cloneCgmes(cgmesSource);
        profiling.end(Operations.TRIPLESTORE_COPY.name());

        String variantId = network.getVariantManager().getWorkingVariantId();

        CgmesUpdate cgmesUpdater = ext.getCgmesUpdater();
        profiling.startLoop();
        if (!cgmesUpdater.changes().isEmpty()) {
            try {
                cgmesUpdater.update(cgmes, variantId, profiling);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        profiling.endLoop(Operations.TRIPLESTORE_UPDATE.name());

        profiling.start();
        // Clear the previous SV data
        cgmes.clear(CgmesSubset.STATE_VARIABLES);
        // Fill the SV data of the CgmesModel with the network current state
        addStateVariables(network, cgmes);
        profiling.end(Operations.ADD_STATE_VARIABLES.name());
        profiling.report();

        profiling.start();
        cgmes.write(ds);
        profiling.end(Operations.WRITE_UPDATED_CGMES.name());
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return "CGMES";
    }

    public enum Operations {
        IMPORT_CGMES, SCALING, LOAD_FLOW, TRIPLESTORE_COPY, CLONE_VARIANT, TRIPLESTORE_UPDATE,
        ADD_STATE_VARIABLES, WRITE_UPDATED_CGMES, CGMES_READ, CGMES_CONVERSION;
    }

    private void addStateVariables(Network n, CgmesModel cgmes) {
        // TODO Add full model data with proper profile (StateVariables)
        // FullModel is defined in ModelDescription:
        // http://iec.ch/TC57/61970-552/ModelDescription/1#

        PropertyBags voltages = new PropertyBags();
        for (Bus b : n.getBusBreakerView().getBuses()) {
            PropertyBag p = new PropertyBag(SV_VOLTAGE_PROPERTIES);
            p.put(CgmesNames.ANGLE, fs(b.getAngle()));
            p.put(CgmesNames.VOLTAGE, fs(b.getV()));
            p.put("TopologicalNode", topologicalNodeFromBusId(b.getId()));
            voltages.add(p);
        }
        cgmes.add(CgmesSubset.STATE_VARIABLES, "SvVoltage", voltages);

        PropertyBags powerFlows = new PropertyBags();
        for (Load l : n.getLoads()) {
            PropertyBag p = createPowerFlowProperties(cgmes, l.getTerminal());
            if (p != null) {
                powerFlows.add(p);
            } else {
                System.err.println("No SvPowerFlow created for load " + l.getId());
            }
        }
        for (Generator g : n.getGenerators()) {
            PropertyBag p = createPowerFlowProperties(cgmes, g.getTerminal());
            if (p != null) {
                powerFlows.add(p);
            }
        }
        for (ShuntCompensator s : n.getShuntCompensators()) {
            PropertyBag p = createPowerFlowProperties(cgmes, s.getTerminal());
            if (p != null) {
                powerFlows.add(createPowerFlowProperties(cgmes, s.getTerminal()));
            }
        }
        cgmes.add(CgmesSubset.STATE_VARIABLES, "SvPowerFlow", powerFlows);

        PropertyBags shuntCompensatorSections = new PropertyBags();
        for (ShuntCompensator s : n.getShuntCompensators()) {
            PropertyBag p = new PropertyBag(SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES);
            p.put("continuousSections", is(s.getCurrentSectionCount()));
            p.put("ShuntCompensator", s.getId());
            shuntCompensatorSections.add(p);
        }
        cgmes.add(CgmesSubset.STATE_VARIABLES, "SvShuntCompensatorSections", shuntCompensatorSections);

        PropertyBags tapSteps = new PropertyBags();
        final List<String> svTapStepProperties = Arrays.asList(nameTapChangerPosition(cgmes),
            CgmesNames.TAP_CHANGER);
        for (TwoWindingsTransformer t : n.getTwoWindingsTransformers()) {
            PropertyBag p = new PropertyBag(svTapStepProperties);
            // TODO If we could store an identifier for the tap changer in IIDM
            // then we would not need to query the CGMES model
            if (t.getPhaseTapChanger() != null) {
                p.put(nameTapChangerPosition(cgmes), is(t.getPhaseTapChanger().getTapPosition()));
                p.put(CgmesNames.TAP_CHANGER, cgmes.phaseTapChangerForPowerTransformer(t.getId()));
                tapSteps.add(p);
            } else if (t.getRatioTapChanger() != null) {
                p.put(nameTapChangerPosition(cgmes), is(t.getRatioTapChanger().getTapPosition()));
                p.put(CgmesNames.TAP_CHANGER, cgmes.ratioTapChangerForPowerTransformer(t.getId()));
                tapSteps.add(p);
            }
        }
        cgmes.add(CgmesSubset.STATE_VARIABLES, "SvTapStep", tapSteps);
    }

    private String nameTapChangerPosition(CgmesModel cgmes) {
        return (cgmes.getCimNamespace().indexOf("cim14#") != -1)
            ? CgmesNames.CONTINUOUS_POSITION
            : CgmesNames.POSITION;
    }

    private PropertyBag createPowerFlowProperties(CgmesModel cgmes, Terminal terminal) {
        // TODO If we could store a terminal identifier in IIDM
        // we would not need to obtain it querying CGMES for the related equipment
        String cgmesTerminal = cgmes.terminalForEquipment(terminal.getConnectable().getId());
        if (cgmesTerminal == null) {
            return null;
        }
        PropertyBag p = new PropertyBag(SV_POWERFLOW_PROPERTIES);
        p.put("p", fs(terminal.getP()));
        p.put("q", fs(terminal.getQ()));
        p.put(CgmesNames.TERMINAL, cgmesTerminal);
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

    private static final List<String> SV_VOLTAGE_PROPERTIES = Arrays.asList(CgmesNames.ANGLE, CgmesNames.VOLTAGE,
        "TopologicalNode");
    private static final List<String> SV_POWERFLOW_PROPERTIES = Arrays.asList("p", "q", CgmesNames.TERMINAL);
    private static final List<String> SV_SHUNTCOMPENSATORSECTIONS_PROPERTIES = Arrays.asList("ShuntCompensator",
        "continuousSections");

    private Profiling profiling;
}
