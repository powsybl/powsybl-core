package com.powsybl.cgmes.model.test;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import org.joda.time.DateTime;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.Subset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

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

    public FakeCgmesModel terminalLimits(String... ids) {
        fakeObjectsFromIdentifiers("OperationalLimit", ids, terminalLimits);
        return this;
    }

    public FakeCgmesModel topologicalNodes(String... ids) {
        fakeObjectsFromIdentifiers("TopologicalNode", ids, topologicalNodes);
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
    public void clear(Subset subset) {
        // FakeCgmesModel, no need to implement clear
    }

    @Override
    public void add(String contextName, String type, PropertyBags objects) {
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
}
