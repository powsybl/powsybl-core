/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.model;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import java.time.ZonedDateTime;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class InMemoryCgmesModel implements CgmesModel {
    private final Properties properties;
    private String modelId;
    private String version;
    private boolean isNodeBreaker;
    private ZonedDateTime created;
    private ZonedDateTime scenarioTime;
    private PropertyBags substations;
    private PropertyBags voltageLevels;
    private PropertyBags terminals;
    private PropertyBags operationalLimits;
    private PropertyBags connectivityNodes;
    private PropertyBags topologicalNodes;
    private PropertyBags busbarSections;
    private PropertyBags switches;
    private PropertyBags acLineSegments;
    private PropertyBags equivalentBranches;
    private PropertyBags seriesCompensators;
    private PropertyBags transformers;
    private PropertyBags transformerEnds;
    private PropertyBags ratioTapChangers;
    private PropertyBags phaseTapChangers;
    private PropertyBags regulatingControls;
    private PropertyBags energyConsumers;
    private PropertyBags energySources;
    private PropertyBags shuntCompensators;
    private PropertyBags staticVarCompensators;
    private PropertyBags equivalentShunts;
    private PropertyBags synchronousMachinesGenerators;
    private PropertyBags equivalentInjections;
    private PropertyBags externalNetworkInjections;
    private PropertyBags svInjections;
    private PropertyBags asynchronousMachines;
    private PropertyBags acDcConverters;
    private PropertyBags dcLineSegments;
    private PropertyBags dcTerminals;
    private PropertyBags controlAreas;
    private PropertyBags tieFlows;
    private PropertyBags numObjectsByType;
    private PropertyBags modelProfiles;

    public InMemoryCgmesModel() {
        properties = new Properties();
        modelId = "fakeModel0";
        version = "unknown";
        isNodeBreaker = false;
        created = ZonedDateTime.now();
        scenarioTime = ZonedDateTime.now();
        substations = new PropertyBags();
        voltageLevels = new PropertyBags();
        terminals = new PropertyBags();
        operationalLimits = new PropertyBags();
        connectivityNodes = new PropertyBags();
        topologicalNodes = new PropertyBags();
        busbarSections = new PropertyBags();
        switches = new PropertyBags();
        acLineSegments = new PropertyBags();
        equivalentBranches = new PropertyBags();
        seriesCompensators = new PropertyBags();
        transformers = new PropertyBags();
        transformerEnds = new PropertyBags();
        ratioTapChangers = new PropertyBags();
        phaseTapChangers = new PropertyBags();
        regulatingControls = new PropertyBags();
        energyConsumers = new PropertyBags();
        energySources = new PropertyBags();
        shuntCompensators = new PropertyBags();
        equivalentShunts = new PropertyBags();
        staticVarCompensators = new PropertyBags();
        synchronousMachinesGenerators = new PropertyBags();
        equivalentInjections = new PropertyBags();
        externalNetworkInjections = new PropertyBags();
        svInjections = new PropertyBags();
        asynchronousMachines = new PropertyBags();
        acDcConverters = new PropertyBags();
        dcLineSegments = new PropertyBags();
        dcTerminals = new PropertyBags();
        controlAreas = new PropertyBags();
        tieFlows = new PropertyBags();
        numObjectsByType = new PropertyBags();
        modelProfiles = new PropertyBags();
    }

    @Override
    public TripleStore tripleStore() {
        return new EmptyTripleStore();
    }

    public boolean hasEquipmentCore() {
        return true;
    }

    public InMemoryCgmesModel modelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    public InMemoryCgmesModel version(String version) {
        this.version = version;
        return this;
    }

    public InMemoryCgmesModel nodeBreaker(boolean b) {
        isNodeBreaker = b;
        return this;
    }

    public boolean isNodeBreaker() {
        return isNodeBreaker;
    }

    public boolean hasBoundary() {
        return false;
    }

    public InMemoryCgmesModel substations(String... ids) {
        fakeObjectsFromIdentifiers("Substation", ids, substations);
        // Add a default SubRegion to every substation
        substations.forEach(s -> {
            s.put("SubRegion", "SubRegion0");
            s.put("subRegionName", "SubRegionName0");
            s.put("Region", "Region0");
            s.put("regionName", "regionName0");
        });
        return this;
    }

    public InMemoryCgmesModel voltageLevels(String... ids) {
        fakeObjectsFromIdentifiers("VoltageLevel", ids, voltageLevels);
        return this;
    }

    public InMemoryCgmesModel terminals(String... ids) {
        fakeObjectsFromIdentifiers("Terminal", ids, terminals);
        return this;
    }

    public InMemoryCgmesModel operationalLimits(String... ids) {
        fakeObjectsFromIdentifiers("OperationalLimit", ids, operationalLimits);
        return this;
    }

    public InMemoryCgmesModel topologicalNodes(String... ids) {
        fakeObjectsFromIdentifiers("TopologicalNode", ids, topologicalNodes);
        return this;
    }

    public InMemoryCgmesModel busBarSections(String... ids) {
        fakeObjectsFromIdentifiers("BusbarSection", ids, busbarSections);
        return this;
    }

    public InMemoryCgmesModel switches(String... ids) {
        fakeObjectsFromIdentifiers("Switch", ids, switches);
        return this;
    }

    public InMemoryCgmesModel acLineSegments(String... ids) {
        fakeObjectsFromIdentifiers("ACLineSegment", ids, acLineSegments);
        return this;
    }

    @Override
    public PropertyBags equivalentBranches() {
        return equivalentBranches;
    }

    @Override
    public PropertyBags seriesCompensators() {
        return seriesCompensators;
    }

    public InMemoryCgmesModel transformers(String... ids) {
        fakeObjectsFromIdentifiers("PowerTransformer", ids, transformers);
        return this;
    }

    public InMemoryCgmesModel transformerEnds(String... ids) {
        fakeObjectsFromIdentifiers("TransformerEnd", ids, transformerEnds);
        return this;
    }

    public InMemoryCgmesModel ratioTapChangers(String... ids) {
        fakeObjectsFromIdentifiers("RatioTapChanger", ids, ratioTapChangers);
        return this;
    }

    public InMemoryCgmesModel phaseTapChangers(String... ids) {
        fakeObjectsFromIdentifiers("PhaseTapChanger", ids, phaseTapChangers);
        return this;
    }

    public InMemoryCgmesModel energyConsumers(String... ids) {
        fakeObjectsFromIdentifiers("EnergyConsumer", ids, energyConsumers);
        return this;
    }

    public InMemoryCgmesModel shuntCompensators(String... ids) {
        fakeObjectsFromIdentifiers("ShuntCompensator", ids, shuntCompensators);
        return this;
    }

    public InMemoryCgmesModel staticVarCompensators(String... ids) {
        fakeObjectsFromIdentifiers("StaticVarCompensator", ids, staticVarCompensators);
        return this;
    }

    public InMemoryCgmesModel synchronousMachinesGenerators(String... ids) {
        fakeObjectsFromIdentifiers("SynchronousMachine", ids, synchronousMachinesGenerators);
        return this;
    }

    public InMemoryCgmesModel asynchronousMachines(String... ids) {
        fakeObjectsFromIdentifiers("AsynchronousMachine", ids, asynchronousMachines);
        return this;
    }

    public InMemoryCgmesModel acDcConverters(String... ids) {
        fakeObjectsFromIdentifiers("ACDCConverter", ids, acDcConverters);
        return this;
    }

    public InMemoryCgmesModel dcLineSegments(String... ids) {
        fakeObjectsFromIdentifiers("DCLineSegment", ids, dcLineSegments);
        return this;
    }

    public InMemoryCgmesModel dcTerminals(String... ids) {
        fakeObjectsFromIdentifiers("DCTerminal", ids, dcTerminals);
        return this;
    }

    public InMemoryCgmesModel tieFlows(String... ids) {
        fakeObjectsFromIdentifiers("TieFlow", ids, tieFlows);
        return this;
    }

    public InMemoryCgmesModel modelProfiles(String... ids) {
        fakeObjectsFromIdentifiers("FullModel", ids, modelProfiles);
        return this;
    }

    private void fakeObjectsFromIdentifiers(String propertyNameId, String[] ids, PropertyBags objects) {
        String[] propertyNames = {propertyNameId};
        for (String id : ids) {
            PropertyBag p = new PropertyBag(Arrays.asList(propertyNames), true);
            p.put(propertyNameId, id);
            objects.add(p);
        }
        PropertyBag p = new PropertyBag(Arrays.asList("Type", "numObjects"), true);
        p.put("Type", propertyNameId);
        p.put("numObjects", "" + ids.length);
        numObjectsByType.add(p);
    }

    @Override
    public PropertyBags fullModel(String cgmesProfile) {
        return new PropertyBags();
    }

    @Override
    public PropertyBags controlAreas() {
        return controlAreas;
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
    public ZonedDateTime scenarioTime() {
        return scenarioTime;
    }

    @Override
    public ZonedDateTime created() {
        return created;
    }

    @Override
    public PropertyBags boundaryNodes() {
        // No need to support boundary nodes in FakeCgmesModel
        return new PropertyBags();
    }

    @Override
    public PropertyBags baseVoltages() {
        return new PropertyBags();
    }

    @Override
    public PropertyBags numObjectsByType() {
        return numObjectsByType;
    }

    @Override
    public PropertyBags allObjectsOfType(String type) {
        return new PropertyBags();
    }

    @Override
    public PropertyBags countrySourcingActors(String countryName) {
        return new PropertyBags();
    }

    @Override
    public PropertyBags sourcingActor(String sourcingActor) {
        return new PropertyBags();
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
    public PropertyBags connectivityNodeContainers() {
        return new PropertyBags();
    }

    @Override
    public PropertyBags operationalLimits() {
        return operationalLimits;
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
    public PropertyBags busBarSections() {
        return busbarSections;
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
        // FakeCgmesModeldoes not provide grouped transformer ends
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
    public PropertyBags regulatingControls() {
        return regulatingControls;
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
    public PropertyBags equivalentShunts() {
        return equivalentShunts;
    }

    @Override
    public PropertyBags nonlinearShuntCompensatorPoints(String scId) {
        return new PropertyBags();
    }

    @Override
    public PropertyBags staticVarCompensators() {
        return staticVarCompensators;
    }

    @Override
    public PropertyBags synchronousMachinesGenerators() {
        return synchronousMachinesGenerators;
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
    public PropertyBags svInjections() {
        return svInjections;
    }

    @Override
    public PropertyBags asynchronousMachines() {
        return asynchronousMachines;
    }

    @Override
    public PropertyBags modelProfiles() {
        return modelProfiles;
    }

    @Override
    public PropertyBags reactiveCapabilityCurveData() {
        // FakeCgmesModel does not implement reactive capability curve
        return new PropertyBags();
    }

    @Override
    public PropertyBags ratioTapChangerTablesPoints() {
        // FakeCgmesModel does not implement ratio tap changer tables
        return new PropertyBags();
    }

    @Override
    public PropertyBags phaseTapChangerTablesPoints() {
        // FakeCgmesModel does not implement phase tap changer tables
        return new PropertyBags();
    }

    @Override
    public PropertyBags ratioTapChangerTable(String tableId) {
        // FakeCgmesModel does not implement ratio tap changer tables
        return new PropertyBags();
    }

    @Override
    public PropertyBags phaseTapChangerTable(String tableId) {
        // FakeCgmesModel does not implement phase tap changer tables
        return new PropertyBags();
    }

    @Override
    public List<String> ratioTapChangerListForPowerTransformer(String powerTransformerId) {
        return Collections.emptyList();
    }

    @Override
    public List<String> phaseTapChangerListForPowerTransformer(String powerTransformerId) {
        return Collections.emptyList();
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
    public PropertyBags tieFlows() {
        return tieFlows;
    }

    @Override
    public PropertyBags topologicalIslands() {
        return new PropertyBags();
    }

    @Override
    public PropertyBags graph() {
        return new PropertyBags();
    }

    @Override
    public void print(PrintStream out) {
        // FakeCgmesModel, no need to implement dump
    }

    @Override
    public void print(Consumer<String> liner) {
        // FakeCgmesModel, no need to implement dump
    }

    @Override
    public void write(DataSource ds) {
        // FakeCgmesModel, no need to implement write
    }

    @Override
    public void write(DataSource ds, CgmesSubset subset) {
        // FakeCgmesModel, no need to implement write
    }

    @Override
    public void clear(CgmesSubset subset) {
        // FakeCgmesModel, no need to implement clear
    }

    @Override
    public void add(CgmesSubset subset, String type, PropertyBags objects) {
     // FakeCgmesModel, no need to implement storage of objects
    }

    @Override
    public void add(String contextOrSubset, String type, PropertyBags objects) {
     // FakeCgmesModel, no need to implement storage of objects
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public Collection<CgmesTerminal> computedTerminals() {
        // FakeCgmesModel does not provide info on terminals
        return Collections.emptyList();
    }

    @Override
    public CgmesTerminal terminal(String terminalId) {
        // FakeCgmesModel does not provide info on terminals
        return null;
    }

    @Override
    public CgmesDcTerminal dcTerminal(String dcTerminalId) {
        // FakeCgmesModel does not provide info on dcTerminals
        return null;
    }

    @Override
    public String substation(CgmesTerminal t, boolean nodeBreaker) {
        return null;
    }

    @Override
    public String voltageLevel(CgmesTerminal t, boolean nodeBreaker) {
        return null;
    }

    @Override
    public CgmesContainer container(String containerId) {
        return null;
    }

    @Override
    public double nominalVoltage(String baseVoltageId) {
        return Double.NaN;
    }

    @Override
    public void setBasename(String baseName) {
        // Not required by current tests
    }

    @Override
    public String getBasename() {
        return null;
    }

    @Override
    public void read(ReadOnlyDataSource ds, ReportNode reportNode) {
        // Not required by current tests
    }

    @Override
    public void read(ReadOnlyDataSource mainDataSource, ReadOnlyDataSource alternativeDataSourceForBoundary, ReportNode reportNode) {
        // Not required by current tests
    }

    @Override
    public void read(InputStream is, String baseName, String contextName, ReportNode reportNode) {
        // Not required by current tests
    }
}
