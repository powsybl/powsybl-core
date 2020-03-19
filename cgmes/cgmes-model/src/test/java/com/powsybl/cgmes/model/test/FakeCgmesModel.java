package com.powsybl.cgmes.model.test;

import com.powsybl.cgmes.model.CgmesContainer;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import org.joda.time.DateTime;
import org.mockito.Mockito;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

public final class FakeCgmesModel implements CgmesModel {
    private final Properties properties;
    private String modelId;
    private String version;
    private boolean isNodeBreaker;
    private DateTime created;
    private DateTime scenarioTime;
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
    private PropertyBags synchronousMachines;
    private PropertyBags equivalentInjections;
    private PropertyBags externalNetworkInjections;
    private PropertyBags svInjections;
    private PropertyBags asynchronousMachines;
    private PropertyBags acDcConverters;
    private PropertyBags dcLineSegments;
    private PropertyBags dcTerminals;
    private PropertyBags numObjectsByType;
    private PropertyBags modelProfiles;

    public FakeCgmesModel() {
        properties = new Properties();
        modelId = "fakeModel0";
        version = "unknown";
        isNodeBreaker = false;
        created = DateTime.now();
        scenarioTime = DateTime.now();
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
        synchronousMachines = new PropertyBags();
        equivalentInjections = new PropertyBags();
        externalNetworkInjections = new PropertyBags();
        svInjections = new PropertyBags();
        asynchronousMachines = new PropertyBags();
        acDcConverters = new PropertyBags();
        dcLineSegments = new PropertyBags();
        dcTerminals = new PropertyBags();
        numObjectsByType = new PropertyBags();
        modelProfiles = new PropertyBags();
    }

    @Override
    public TripleStore tripleStore() {
        return Mockito.mock(TripleStore.class);
    }

    public boolean hasEquipmentCore() {
        return true;
    }

    public FakeCgmesModel modelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    public FakeCgmesModel version(String version) {
        this.version = version;
        return this;
    }

    public FakeCgmesModel nodeBreaker(boolean b) {
        isNodeBreaker = b;
        return this;
    }

    public boolean isNodeBreaker() {
        return isNodeBreaker;
    }

    public boolean hasBoundary() {
        return false;
    }

    public FakeCgmesModel substations(String... ids) {
        fakeObjectsFromIdentifiers("Substation", ids, substations);
        // Add a default SubRegion to every substation
        substations.forEach(s -> s.put("SubRegion", "SubRegion0"));
        return this;
    }

    public FakeCgmesModel voltageLevels(String... ids) {
        fakeObjectsFromIdentifiers("VoltageLevel", ids, voltageLevels);
        return this;
    }

    public FakeCgmesModel terminals(String... ids) {
        fakeObjectsFromIdentifiers("Terminal", ids, terminals);
        return this;
    }

    public FakeCgmesModel operationalLimits(String... ids) {
        fakeObjectsFromIdentifiers("OperationalLimit", ids, operationalLimits);
        return this;
    }

    public FakeCgmesModel topologicalNodes(String... ids) {
        fakeObjectsFromIdentifiers("TopologicalNode", ids, topologicalNodes);
        return this;
    }

    public FakeCgmesModel busbarSections(String... ids) {
        fakeObjectsFromIdentifiers("BusbarSection", ids, busbarSections);
        return this;
    }

    public FakeCgmesModel switches(String... ids) {
        fakeObjectsFromIdentifiers("Switch", ids, switches);
        return this;
    }

    public FakeCgmesModel acLineSegments(String... ids) {
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

    public FakeCgmesModel transformers(String... ids) {
        fakeObjectsFromIdentifiers("PowerTransformer", ids, transformers);
        return this;
    }

    public FakeCgmesModel transformerEnds(String... ids) {
        fakeObjectsFromIdentifiers("TransformerEnd", ids, transformerEnds);
        return this;
    }

    public FakeCgmesModel ratioTapChangers(String... ids) {
        fakeObjectsFromIdentifiers("RatioTapChanger", ids, ratioTapChangers);
        return this;
    }

    public FakeCgmesModel phaseTapChangers(String... ids) {
        fakeObjectsFromIdentifiers("PhaseTapChanger", ids, phaseTapChangers);
        return this;
    }

    public FakeCgmesModel energyConsumers(String... ids) {
        fakeObjectsFromIdentifiers("EnergyConsumer", ids, energyConsumers);
        return this;
    }

    public FakeCgmesModel shuntCompensators(String... ids) {
        fakeObjectsFromIdentifiers("ShuntCompensator", ids, shuntCompensators);
        return this;
    }

    public FakeCgmesModel staticVarCompensators(String... ids) {
        fakeObjectsFromIdentifiers("StaticVarCompensator", ids, staticVarCompensators);
        return this;
    }

    public FakeCgmesModel synchronousMachines(String... ids) {
        fakeObjectsFromIdentifiers("SynchronousMachine", ids, synchronousMachines);
        return this;
    }

    public FakeCgmesModel asynchronousMachines(String... ids) {
        fakeObjectsFromIdentifiers("AsynchronousMachine", ids, asynchronousMachines);
        return this;
    }

    public FakeCgmesModel acDcConverters(String... ids) {
        fakeObjectsFromIdentifiers("ACDCConverter", ids, acDcConverters);
        return this;
    }

    public FakeCgmesModel dcLineSegments(String... ids) {
        fakeObjectsFromIdentifiers("DCLineSegment", ids, dcLineSegments);
        return this;
    }

    public FakeCgmesModel dcTerminals(String... ids) {
        fakeObjectsFromIdentifiers("DCTerminal", ids, dcTerminals);
        return this;
    }

    public FakeCgmesModel modelProfiles(String... ids) {
        fakeObjectsFromIdentifiers("FullModel", ids, modelProfiles);
        return this;
    }

    private void fakeObjectsFromIdentifiers(String propertyNameId, String[] ids, PropertyBags objects) {
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
    public PropertyBags fullModel(String cgmesProfile) {
        return null;
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
        // No need to support boundary nodes in FakeCgmesModel
        return null;
    }

    @Override
    public PropertyBags baseVoltages() {
        // No need to support base voltages in FakeCgmesModel
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
    public PropertyBags connectivityNodeContainers() {
        // TODO(Luma) refactoring node-breaker conversion temporal
        return null;
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
        return null;
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
        return null;
    }

    @Override
    public PropertyBags ratioTapChangerTablesPoints() {
        // FakeCgmesModel does not implement ratio tap changer tables
        return null;
    }

    @Override
    public PropertyBags phaseTapChangerTablesPoints() {
        // FakeCgmesModel does not implement phase tap changer tables
        return null;
    }

    @Override
    public PropertyBags ratioTapChangerTable(String tableId) {
        // FakeCgmesModel does not implement ratio tap changer tables
        return null;
    }

    @Override
    public PropertyBags phaseTapChangerTable(String tableId) {
        // FakeCgmesModel does not implement phase tap changer tables
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
    public PropertyBags topologicalIslands() {
        return null;
    }

    @Override
    public PropertyBags graph() {
        return null;
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
    public CgmesTerminal terminal(String terminalId) {
        // FakeCgmesModel does not provide info on terminals
        return null;
    }

    @Override
    public String terminalForEquipment(String conductingEquipmentId) {
        return null;
    }

    @Override
    public String ratioTapChangerForPowerTransformer(String powerTransformerId) {
        return null;
    }

    @Override
    public String phaseTapChangerForPowerTransformer(String powerTransformerId) {
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
        // TODO Review if required by current tests
    }

    @Override
    public String getBasename() {
        // TODO Review if required by current tests
        return null;
    }

    @Override
    public void read(ReadOnlyDataSource ds) {
        // TODO Review if required by current tests
    }

    @Override
    public void read(ReadOnlyDataSource mainDataSource, ReadOnlyDataSource alternativeDataSourceForBoundary) {
        // TODO Review if required by current tests
    }

    @Override
    public void read(InputStream is, String baseName, String contextName) {
        // TODO Review if required by current tests
    }
}
