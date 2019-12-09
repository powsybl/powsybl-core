/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.triplestore;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.AbstractCgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.QueryCatalog;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesModelTripleStore extends AbstractCgmesModel {

    public CgmesModelTripleStore(String cimNamespace, TripleStore tripleStore) {
        this.cimNamespace = cimNamespace;
        this.tripleStore = tripleStore;
        tripleStore.defineQueryPrefix("cim", cimNamespace);
        tripleStore.defineQueryPrefix("entsoe", CgmesNamespace.ENTSOE_NAMESPACE);
        queryCatalog = queryCatalogFor(cimNamespace);
        Objects.requireNonNull(queryCatalog);
    }

    @Override
    public void read(InputStream is, String baseName, String contextName) {
        tripleStore.read(is, baseName, contextName);
    }

    @Override
    public void print(PrintStream out) {
        tripleStore.print(out);
    }

    @Override
    public void print(Consumer<String> liner) {
        tripleStore.print(liner);
    }

    @Override
    public void write(DataSource ds) {
        try {
            tripleStore.write(ds);
        } catch (TripleStoreException x) {
            throw new CgmesModelException(String.format("Writing. Triple store problem %s", ds), x);
        }
    }

    // Queries

    @Override
    public boolean hasEquipmentCore() {
        if (queryCatalog.containsKey(MODEL_PROFILES)) {
            PropertyBags r = namedQuery(MODEL_PROFILES);
            if (r == null) {
                return false;
            }
            for (PropertyBag m : r) {
                String p = m.get(PROFILE);
                if (p != null && p.contains("/EquipmentCore/")) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Model contains Equipment Core data profile in model {}",
                                m.get(CgmesNames.FULL_MODEL));
                    }
                    return true;
                }
            }

            // We have a query for model profiles
            // but none of the FullModel objects contains EquipmentCore profile
            return false;
        }
        // If we do not have a query for model profiles we assume equipment core is
        // available
        // (This covers the case for CIM14 files)
        return true;
    }

    @Override
    public boolean hasBoundary() {
        // The Model has boundary if we are able to find models
        // that have EquipmentBoundary profile
        // and models that have TopologyBoundary profile
        boolean hasEquipmentBoundary = false;
        boolean hasTopologyBoundary = false;
        if (queryCatalog.containsKey(MODEL_PROFILES)) {
            PropertyBags r = namedQuery(MODEL_PROFILES);
            if (r == null) {
                return false;
            }
            for (PropertyBag m : r) {
                String p = m.get(PROFILE);
                String mid = m.get(CgmesNames.FULL_MODEL);
                if (p != null && p.contains("/EquipmentBoundary/")) {
                    LOG.info("Model contains EquipmentBoundary data in model {}", mid);
                    hasEquipmentBoundary = true;
                }
                if (p != null && p.contains("/TopologyBoundary/")) {
                    LOG.info("Model contains TopologyBoundary data in model {}", mid);
                    hasTopologyBoundary = true;
                }
            }
        }
        // If we do not have a query for model profiles we assume no boundary exist
        // (Maybe for CIM14 data sources we should rely on file names ?)
        return hasEquipmentBoundary && hasTopologyBoundary;
    }

    @Override
    public boolean isNodeBreaker() {
        // Optimization hint: consider caching the results of the query for model
        // profiles
        if (!queryCatalog.containsKey(MODEL_PROFILES)) {
            return false;
        }
        PropertyBags r = namedQuery(MODEL_PROFILES);
        if (r == null) {
            return false;
        }
        // Only consider is node breaker if all models that have profile
        // EquipmentCore or EquipmentBoundary
        // also have EquipmentOperation or EquipmentBoundaryOperation
        Map<String, Boolean> eqModelHasEquipmentOperationProfile = new HashMap<>();
        for (PropertyBag mp : r) {
            String m = mp.get("FullModel");
            String p = mp.get(PROFILE);
            if (p != null) {
                if (p.contains("/EquipmentCore/") || p.contains("/EquipmentBoundary/")) {
                    eqModelHasEquipmentOperationProfile.putIfAbsent(m, false);
                }
                if (p.contains("/EquipmentOperation/") || p.contains("/EquipmentBoundaryOperation/")) {
                    eqModelHasEquipmentOperationProfile.put(m, true);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Model {} is considered node-breaker", m);
                    }
                }
            }
        }
        boolean isNodeBreaker = eqModelHasEquipmentOperationProfile.values().stream().allMatch(Boolean::valueOf);
        if (isNodeBreaker) {
            LOG.info(
                    "All FullModel objects have EquipmentOperation profile, so conversion will be considered node-breaker");
        } else {
            LOG.info(
                    "Following FullModel objects do not have EquipmentOperation profile, so conversion will not be considered node-breaker:");
            eqModelHasEquipmentOperationProfile.entrySet().forEach(meqop -> {
                if (!meqop.getValue()) {
                    LOG.info("    {}", meqop.getKey());
                }
            });
        }
        return isNodeBreaker;
    }

    @Override
    public String modelId() {
        String modelId = "unknown";
        if (queryCatalog.containsKey("modelIds")) {
            PropertyBags r = namedQuery("modelIds");
            if (r != null && !r.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Candidates to model identifier:{}{}", System.lineSeparator(), r.tabulateLocals());
                }
                String v = r.get(0).get("FullModel");
                if (v != null) {
                    modelId = v;
                }
            }
        }
        return modelId;
    }

    @Override
    public DateTime scenarioTime() {
        DateTime defaultScenarioTime = DateTime.now();
        return queryDate("scenarioTime", defaultScenarioTime);
    }

    @Override
    public DateTime created() {
        DateTime defaultCreated = DateTime.now();
        return queryDate("created", defaultCreated);
    }

    private DateTime queryDate(String propertyName, DateTime defaultValue) {
        DateTime d = defaultValue;
        if (queryCatalog.containsKey("modelDates")) {
            PropertyBags r = namedQuery("modelDates");
            if (r != null && !r.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Candidates to modelDates:{}{}", System.lineSeparator(), r.tabulateLocals());
                }
                String s = r.get(0).get(propertyName);
                if (s != null && !s.isEmpty()) {
                    d = DateTime.parse(s);
                }
            }
        }
        return d;
    }

    @Override
    public String version() {
        String version = "unknown";
        PropertyBags r = namedQuery("version");
        if (r != null && !r.isEmpty()) {
            String v = r.get(0).get("version");
            if (v != null) {
                version = v;
            }
        }
        return version;
    }

    @Override
    public PropertyBags numObjectsByType() {
        Objects.requireNonNull(cimNamespace);
        return namedQuery("numObjectsByType", cimNamespace);
    }

    @Override
    public PropertyBags allObjectsOfType(String type) {
        Objects.requireNonNull(type);
        return namedQuery("allObjectsOfType", type);
    }

    @Override
    public PropertyBags boundaryNodes() {
        return namedQuery("boundaryNodes");
    }

    @Override
    public PropertyBags baseVoltages() {
        return namedQuery("baseVoltages");
    }

    @Override
    public PropertyBags substations() {
        return namedQuery("substations");
    }

    @Override
    public PropertyBags voltageLevels() {
        return namedQuery("voltageLevels");
    }

    @Override
    public PropertyBags terminals() {
        return namedQuery("terminals");
    }

    @Override
    public PropertyBags connectivityNodes() {
        return namedQuery("connectivityNodes");
    }

    @Override
    public PropertyBags topologicalNodes() {
        return namedQuery("topologicalNodes");
    }

    @Override
    public PropertyBags connectivityNodeContainers() {
        return namedQuery("connectivityNodeContainers");
    }

    @Override
    public PropertyBags operationalLimits() {
        return namedQuery("operationalLimits");
    }

    @Override
    public PropertyBags busBarSections() {
        return namedQuery("busbarSections");
    }

    @Override
    public PropertyBags switches() {
        return namedQuery("switches");
    }

    @Override
    public PropertyBags acLineSegments() {
        return namedQuery("acLineSegments");
    }

    @Override
    public PropertyBags equivalentBranches() {
        return namedQuery("equivalentBranches");
    }

    @Override
    public PropertyBags seriesCompensators() {
        return namedQuery("seriesCompensators");
    }

    @Override
    public PropertyBags transformers() {
        return namedQuery("transformers");
    }

    @Override
    public PropertyBags transformerEnds() {
        return namedQuery("transformerEnds");
    }

    @Override
    public PropertyBags ratioTapChangers() {
        return namedQuery("ratioTapChangers");
    }

    @Override
    public PropertyBags phaseTapChangers() {
        return namedQuery("phaseTapChangers");
    }

    @Override
    public PropertyBags regulatingControls() {
        return namedQuery("regulatingControls");
    }

    @Override
    public PropertyBags energyConsumers() {
        return namedQuery("energyConsumers");
    }

    @Override
    public PropertyBags energySources() {
        return namedQuery("energySources");
    }

    @Override
    public PropertyBags shuntCompensators() {
        return namedQuery("shuntCompensators");
    }

    @Override
    public PropertyBags nonlinearShuntCompensatorPoints(String scId) {
        Objects.requireNonNull(scId);
        return namedQuery("nonlinearShuntCompensatorPoints", scId);
    }

    @Override
    public PropertyBags staticVarCompensators() {
        return namedQuery("staticVarCompensators");
    }

    @Override
    public PropertyBags synchronousMachines() {
        return namedQuery("synchronousMachines");
    }

    @Override
    public PropertyBags equivalentInjections() {
        return namedQuery("equivalentInjections");
    }

    @Override
    public PropertyBags externalNetworkInjections() {
        return namedQuery("externalNetworkInjections");
    }

    @Override
    public PropertyBags svInjections() {
        return namedQuery("svInjections");
    }

    @Override
    public PropertyBags asynchronousMachines() {
        return namedQuery("asynchronousMachines");
    }

    @Override
    public PropertyBags reactiveCapabilityCurveData() {
        return namedQuery("reactiveCapabilityCurveData");
    }

    @Override
    public PropertyBags ratioTapChangerTablesPoints() {
        return namedQuery("ratioTapChangerTablesPoints");
    }

    @Override
    public PropertyBags ratioTapChangerTable(String tableId) {
        Objects.requireNonNull(tableId);
        return namedQuery("ratioTapChangerTable", tableId);
    }

    @Override
    public PropertyBags phaseTapChangerTable(String tableId) {
        Objects.requireNonNull(tableId);
        return namedQuery("phaseTapChangerTable", tableId);
    }

    @Override
    public PropertyBags acDcConverters() {
        return namedQuery("acDcConverters");
    }

    @Override
    public PropertyBags dcLineSegments() {
        return namedQuery("dcLineSegments");
    }

    @Override
    public PropertyBags dcTerminals() {
        return namedQuery("dcTerminals");
    }

    @Override
    public PropertyBags dcTerminalsTP() {
        return namedQuery("dcTerminalsTP");
    }

    @Override
    public PropertyBags modelProfiles() {
        return namedQuery(MODEL_PROFILES);
    }

    public PropertyBags namedQuery(String name, String... params) {
        String queryText = queryCatalog.get(name);
        if (queryText == null) {
            LOG.warn("Query [{}] not found in catalog", name);
            return new PropertyBags();
        }
        // Optimization hint: Now we do the parameter injection by ourselves,
        // to maintain independence of the triple store engine,
        // instead of using native query parameters
        queryText = injectParams(queryText, params);
        final long t0 = System.currentTimeMillis();
        PropertyBags r = query(queryText);
        final long t1 = System.currentTimeMillis();
        if (LOG.isDebugEnabled()) {
            LOG.debug("dt query {} {} ms, result set size = {}", name, t1 - t0, r.size());
        }
        return r;
    }

    public PropertyBags query(String queryText) {
        return tripleStore.query(queryText);
    }

    @Override
    public TripleStore tripleStore() {
        return tripleStore;
    }

    // Updates

    @Override
    public void clear(CgmesSubset subset) {
        // TODO Remove all contexts that are related to the profile of the subset
        // For example for state variables:
        // <md:Model.profile>http://entsoe.eu/CIM/StateVariables/4/1</md:Model.profile>
        // For CIM14 data files we do not have the profile,
        Set<String> contextNames = tripleStore.contextNames();
        for (String contextName : contextNames) {
            if (subset.isValidName(contextName)) {
                tripleStore.clear(contextName);
            }
        }
    }

    @Override
    public void add(CgmesSubset subset, String type, PropertyBags objects) {
        String contextName = contextNameFor(subset);
        try {
            tripleStore.add(contextName, cimNamespace, type, objects);
        } catch (TripleStoreException x) {
            String msg = String.format("Adding objects of type %s to subset %s, context %s", type, subset, contextName);
            throw new CgmesModelException(msg, x);
        }
    }

    private String contextNameFor(CgmesSubset subset) {
        String contextNameEQ = contextNameForEquipmentSubset();
        return contextNameEQ != null
                ? buildContextNameForSubsetFrom(contextNameEQ, subset)
                : modelId() + "_" + subset + ".xml";
    }

    private String contextNameForEquipmentSubset() {
        String eq = CgmesSubset.EQUIPMENT.getIdentifier();
        String eqBD = CgmesSubset.EQUIPMENT_BOUNDARY.getIdentifier();
        for (String contextName : tripleStore.contextNames()) {
            if (contextName.contains(eq) && !contextName.contains(eqBD)) {
                return contextName;
            }
        }
        return null;
    }

    private String buildContextNameForSubsetFrom(String contextNameEQ, CgmesSubset subset) {
        String eq = CgmesSubset.EQUIPMENT.getIdentifier();
        return contextNameEQ.replace(eq, subset.getIdentifier());
    }

    private QueryCatalog queryCatalogFor(String cimNamespace) {
        QueryCatalog qc = null;
        String version = cimNamespace.substring(cimNamespace.lastIndexOf("cim"));
        String resourceName = null;
        if (version.equals("cim14#")) {
            resourceName = "CIM14.sparql";
        } else if (version.equals("cim16#")) {
            resourceName = "CIM16.sparql";
        }
        if (resourceName != null) {
            qc = new QueryCatalog(resourceName);
        }
        return qc;
    }

    private String injectParams(String queryText, String... params) {
        String injected = queryText;
        for (int k = 0; k < params.length; k++) {
            String pkref = "{" + k + "}";
            injected = injected.replace(pkref, params[k]);
        }
        return injected;
    }

    private final String cimNamespace;
    private final TripleStore tripleStore;
    private final QueryCatalog queryCatalog;

    private static final String MODEL_PROFILES = "modelProfiles";
    private static final String PROFILE = "profile";
    private static final Logger LOG = LoggerFactory.getLogger(CgmesModelTripleStore.class);
}
