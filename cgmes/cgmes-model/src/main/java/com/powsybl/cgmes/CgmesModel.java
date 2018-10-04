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

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import org.joda.time.DateTime;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.PropertyBag;
import com.powsybl.triplestore.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public interface CgmesModel {

    Properties getProperties();

    String modelId();

    String version();

    DateTime scenarioTime();

    DateTime created();

    boolean isNodeBreaker();

    CgmesTerminal terminal(String terminalId);

    PropertyBags numObjectsByType();

    PropertyBags allObjectsOfType(String type);

    PropertyBags boundaryNodes();

    PropertyBags baseVoltages();

    PropertyBags substations();

    PropertyBags voltageLevels();

    PropertyBags terminals();

    PropertyBags terminalsTP();

    PropertyBags terminalsCN();

    PropertyBags terminalLimits();

    PropertyBags connectivityNodes();

    PropertyBags topologicalNodes();

    PropertyBags switches();

    PropertyBags acLineSegments();

    PropertyBags equivalentBranches();

    PropertyBags transformers();

    PropertyBags transformerEnds();

    // Transformer ends grouped by transformer
    Map<String, PropertyBags> groupedTransformerEnds();

    PropertyBags ratioTapChangers();

    PropertyBags phaseTapChangers();

    PropertyBags energyConsumers();

    PropertyBags energySources();

    PropertyBags shuntCompensators();

    PropertyBags staticVarCompensators();

    PropertyBags synchronousMachines();

    PropertyBags equivalentInjections();

    PropertyBags externalNetworkInjections();

    PropertyBags asynchronousMachines();

    PropertyBags phaseTapChangerTable(String tableId);

    PropertyBags acDcConverters();

    PropertyBags dcLineSegments();

    PropertyBags dcTerminals();

    PropertyBags dcTerminalsTP();

    void svVoltages(PropertyBags svVoltages);

    void svPowerFlows(PropertyBags svPowerFlows);

    void svShuntCompensatorSections(PropertyBags svShuntCompensatorSections);

    void svTapSteps(PropertyBags svTapSteps);

    void dump(PrintStream out);

    void dump(Consumer<String> liner);

    void write(DataSource ds);

    public static class CgmesTerminal {
        private final String id;
        private final String conductingEquipment;
        private final String conductingEquipmentType;
        private final boolean connected;
        private final PowerFlow flow;

        private String connectivityNode;
        private String topologicalNode;
        private String voltageLevel;
        private String substation;

        public CgmesTerminal(
                String id,
                String conductingEquipment,
                String conductingEquipmentType,
                boolean connected,
                PowerFlow flow) {
            this.id = id;
            this.conductingEquipment = conductingEquipment;
            this.conductingEquipmentType = conductingEquipmentType;
            this.connected = connected;
            this.flow = flow;
        }

        public void assignTP(String topologicalNode, String voltageLevel, String substation) {
            checkAssign(topologicalNode, voltageLevel, substation);
        }

        public void assignCN(String connectivityNode, String topologicalNode, String voltageLevel,
                String substation) {
            this.connectivityNode = connectivityNode;
            checkAssign(topologicalNode, voltageLevel, substation);
        }

        public String id() {
            return id;
        }

        public String conductingEquipment() {
            return conductingEquipment;
        }

        public String conductingEquipmentType() {
            return conductingEquipmentType;
        }

        public String connectivityNode() {
            return connectivityNode;
        }

        public String topologicalNode() {
            return topologicalNode;
        }

        public String voltageLevel() {
            return voltageLevel;
        }

        public String substation() {
            return substation;
        }

        public boolean connected() {
            return connected;
        }

        public PowerFlow flow() {
            return flow;
        }

        private void checkAssign(String topologicalNode, String voltageLevel, String substation) {
            checkAssignAttr("topologicalNode", this.topologicalNode, topologicalNode);
            this.topologicalNode = topologicalNode;
            checkAssignAttr("voltageLevel", this.voltageLevel, voltageLevel);
            this.voltageLevel = voltageLevel;
            checkAssignAttr("substation", this.substation, substation);
            this.substation = substation;
        }

        private void checkAssignAttr(String attribute, String value0, String value1) {
            if (value0 == null || value0.equals(value1)) {
                return;
            }
            throw new CgmesModelException(
                    String.format("Inconsistent values for %s: previous %s, now %s",
                            attribute, value0, value1));
        }
    }

    static final class Fake implements CgmesModel {
        private final Properties properties;
        private String modelId;
        private String version;
        private boolean isNodeBreaker;
        private DateTime created;
        private DateTime scenarioTime;
        private PropertyBags substations;
        private PropertyBags voltageLevels;
        private PropertyBags terminals;
        private PropertyBags terminalLimits;
        private PropertyBags connectivityNodes;
        private PropertyBags topologicalNodes;
        private PropertyBags switches;
        private PropertyBags acLineSegments;
        private PropertyBags equivalentBranches;
        private PropertyBags transformers;
        private PropertyBags transformerEnds;
        private PropertyBags ratioTapChangers;
        private PropertyBags phaseTapChangers;
        private PropertyBags energyConsumers;
        private PropertyBags energySources;
        private PropertyBags shuntCompensators;
        private PropertyBags staticVarCompensators;
        private PropertyBags synchronousMachines;
        private PropertyBags equivalentInjections;
        private PropertyBags externalNetworkInjections;
        private PropertyBags asynchronousMachines;
        private PropertyBags acDcConverters;
        private PropertyBags dcLineSegments;
        private PropertyBags dcTerminals;
        private PropertyBags numObjectsByType;

        public Fake() {
            properties = new Properties();
            modelId = "fakeModel0";
            version = "unknown";
            isNodeBreaker = false;
            created = DateTime.now();
            scenarioTime = DateTime.now();
            substations = new PropertyBags();
            voltageLevels = new PropertyBags();
            terminals = new PropertyBags();
            terminalLimits = new PropertyBags();
            connectivityNodes = new PropertyBags();
            topologicalNodes = new PropertyBags();
            switches = new PropertyBags();
            acLineSegments = new PropertyBags();
            equivalentBranches = new PropertyBags();
            transformers = new PropertyBags();
            transformerEnds = new PropertyBags();
            ratioTapChangers = new PropertyBags();
            phaseTapChangers = new PropertyBags();
            energyConsumers = new PropertyBags();
            energySources = new PropertyBags();
            shuntCompensators = new PropertyBags();
            staticVarCompensators = new PropertyBags();
            synchronousMachines = new PropertyBags();
            equivalentInjections = new PropertyBags();
            externalNetworkInjections = new PropertyBags();
            asynchronousMachines = new PropertyBags();
            acDcConverters = new PropertyBags();
            dcLineSegments = new PropertyBags();
            dcTerminals = new PropertyBags();
            numObjectsByType = new PropertyBags();
        }

        public Fake modelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        public Fake version(String version) {
            this.version = version;
            return this;
        }

        public Fake nodeBreaker(boolean b) {
            isNodeBreaker = b;
            return this;
        }

        public boolean isNodeBreaker() {
            return isNodeBreaker;
        }

        public Fake substations(String... ids) {
            fakeObjectsFromIdentifiers("Substation", ids, substations);
            // Add a default SubRegion to every substation
            substations.stream().forEach(s -> s.put("SubRegion", "SubRegion0"));
            return this;
        }

        public Fake voltageLevels(String... ids) {
            fakeObjectsFromIdentifiers("VoltageLevel", ids, voltageLevels);
            return this;
        }

        public Fake terminals(String... ids) {
            fakeObjectsFromIdentifiers("Terminal", ids, terminals);
            return this;
        }

        public Fake terminalLimits(String... ids) {
            fakeObjectsFromIdentifiers("OperationalLimit", ids, terminalLimits);
            return this;
        }

        public Fake topologicalNodes(String... ids) {
            fakeObjectsFromIdentifiers("TopologicalNode", ids, topologicalNodes);
            return this;
        }

        public Fake switches(String... ids) {
            fakeObjectsFromIdentifiers("Switch", ids, switches);
            return this;
        }

        public Fake acLineSegments(String... ids) {
            fakeObjectsFromIdentifiers("ACLineSegment", ids, acLineSegments);
            return this;
        }

        @Override
        public PropertyBags equivalentBranches() {
            return equivalentBranches;
        }

        public Fake transformers(String... ids) {
            fakeObjectsFromIdentifiers("PowerTransformer", ids, transformers);
            return this;
        }

        public Fake transformerEnds(String... ids) {
            fakeObjectsFromIdentifiers("TransformerEnd", ids, transformerEnds);
            return this;
        }

        public Fake ratioTapChangers(String... ids) {
            fakeObjectsFromIdentifiers("RatioTapChanger", ids, ratioTapChangers);
            return this;
        }

        public Fake phaseTapChangers(String... ids) {
            fakeObjectsFromIdentifiers("PhaseTapChanger", ids, phaseTapChangers);
            return this;
        }

        public Fake energyConsumers(String... ids) {
            fakeObjectsFromIdentifiers("EnergyConsumer", ids, energyConsumers);
            return this;
        }

        public Fake shuntCompensators(String... ids) {
            fakeObjectsFromIdentifiers("ShuntCompensator", ids, shuntCompensators);
            return this;
        }

        public Fake staticVarCompensators(String... ids) {
            fakeObjectsFromIdentifiers("StaticVarCompensator", ids, staticVarCompensators);
            return this;
        }

        public Fake synchronousMachines(String... ids) {
            fakeObjectsFromIdentifiers("SynchronousMachine", ids, synchronousMachines);
            return this;
        }

        public Fake asynchronousMachines(String... ids) {
            fakeObjectsFromIdentifiers("AsynchronousMachine", ids, asynchronousMachines);
            return this;
        }

        public Fake acDcConverters(String... ids) {
            fakeObjectsFromIdentifiers("ACDCConverter", ids, acDcConverters);
            return this;
        }

        public Fake dcLineSegments(String... ids) {
            fakeObjectsFromIdentifiers("DCLineSegment", ids, dcLineSegments);
            return this;
        }

        public Fake dcTerminals(String... ids) {
            fakeObjectsFromIdentifiers("DCTerminal", ids, dcTerminals);
            return this;
        }

        private void fakeObjectsFromIdentifiers(String propertyNameId, String[] ids,
                PropertyBags objects) {
            String[] propertyNames = {propertyNameId};
            for (String id : ids) {
                PropertyBag p = new PropertyBag(Arrays.asList(propertyNames));
                p.put(propertyNameId, id);
                objects.add(p);
            }
            PropertyBag p = new PropertyBag(Arrays.asList("Type", "numObjects"));
            p.put("Type", propertyNameId);
            p.put("numObjects", "" + ids.length);
            numObjectsByType.add(p);
        }

        @Override
        public String modelId() {
            return modelId;
        }

        @Override
        public String version() {
            return version;
        }

        @Override
        public DateTime scenarioTime() {
            return scenarioTime;
        }

        @Override
        public DateTime created() {
            return created;
        }

        @Override
        public PropertyBags boundaryNodes() {
            // No need to support boundary nodes in Fake model
            return null;
        }

        @Override
        public PropertyBags baseVoltages() {
            // No need to support base voltages in Fake model
            return null;
        }

        @Override
        public PropertyBags numObjectsByType() {
            return numObjectsByType;
        }

        @Override
        public PropertyBags allObjectsOfType(String type) {
            return null;
        }

        @Override
        public PropertyBags substations() {
            return substations;
        }

        @Override
        public PropertyBags voltageLevels() {
            return voltageLevels;
        }

        @Override
        public PropertyBags terminals() {
            return terminals;
        }

        @Override
        public PropertyBags terminalsTP() {
            return null;
        }

        @Override
        public PropertyBags terminalsCN() {
            return null;
        }

        @Override
        public PropertyBags terminalLimits() {
            return terminalLimits;
        }

        @Override
        public PropertyBags connectivityNodes() {
            return connectivityNodes;
        }

        @Override
        public PropertyBags topologicalNodes() {
            return topologicalNodes;
        }

        @Override
        public PropertyBags switches() {
            return switches;
        }

        @Override
        public PropertyBags acLineSegments() {
            return acLineSegments;
        }

        @Override
        public PropertyBags transformers() {
            return transformers;
        }

        @Override
        public PropertyBags transformerEnds() {
            return transformerEnds;
        }

        @Override
        public Map<String, PropertyBags> groupedTransformerEnds() {
            // Fake does not provide grouped transformer ends
            return Collections.emptyMap();
        }

        @Override
        public PropertyBags ratioTapChangers() {
            return ratioTapChangers;
        }

        @Override
        public PropertyBags phaseTapChangers() {
            return phaseTapChangers;
        }

        @Override
        public PropertyBags energyConsumers() {
            return energyConsumers;
        }

        @Override
        public PropertyBags energySources() {
            return energySources;
        }

        @Override
        public PropertyBags shuntCompensators() {
            return shuntCompensators;
        }

        @Override
        public PropertyBags staticVarCompensators() {
            return staticVarCompensators;
        }

        @Override
        public PropertyBags synchronousMachines() {
            return synchronousMachines;
        }

        @Override
        public PropertyBags equivalentInjections() {
            return equivalentInjections;
        }

        @Override
        public PropertyBags externalNetworkInjections() {
            return externalNetworkInjections;
        }

        @Override
        public PropertyBags asynchronousMachines() {
            return asynchronousMachines;
        }

        @Override
        public PropertyBags phaseTapChangerTable(String tableId) {
            // Fake model does not implement phase tap changer tables
            return null;
        }

        @Override
        public PropertyBags acDcConverters() {
            return acDcConverters;
        }

        @Override
        public PropertyBags dcLineSegments() {
            return dcLineSegments;
        }

        @Override
        public PropertyBags dcTerminals() {
            return dcTerminals;
        }

        @Override
        public PropertyBags dcTerminalsTP() {
            return dcTerminals;
        }

        @Override
        public void dump(PrintStream out) {
            // Fake model, no need to implement dump
        }

        @Override
        public void dump(Consumer<String> liner) {
            // Fake model, no need to implement dump
        }

        @Override
        public void write(DataSource ds) {
            // Fake model, no need to implement write
        }

        @Override
        public void svVoltages(PropertyBags svVoltages) {
            // Fake model, no need to implement storage of SV voltages
        }

        @Override
        public void svPowerFlows(PropertyBags svPowerFlows) {
            // Fake model, no need to implement storage of SV powerflows
        }

        @Override
        public void svShuntCompensatorSections(PropertyBags svShuntCompensatorSections) {
            // Fake model, no need to implement storage of SV shuntCompensatorSections
        }

        @Override
        public void svTapSteps(PropertyBags svTapSteps) {
            // Fake model, no need to implement storage of SV tapstep
        }

        @Override
        public Properties getProperties() {
            return properties;
        }

        @Override
        public CgmesTerminal terminal(String terminalId) {
            // Fake model does not provide info on terminals
            return null;
        }
    }
}
