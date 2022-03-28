/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.triplestore;

import com.powsybl.cgmes.model.*;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.api.*;
import org.apache.commons.lang3.EnumUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesModelTripleStore extends AbstractCgmesModel {

    public CgmesModelTripleStore(String cimNamespace, TripleStore tripleStore) {
        this.cimNamespace = cimNamespace;
        this.cimVersion = cimVersionFromCimNamespace(cimNamespace);
        this.tripleStore = tripleStore;
        tripleStore.defineQueryPrefix("cim", cimNamespace);
        tripleStore.defineQueryPrefix("entsoe", CgmesNamespace.ENTSOE_NAMESPACE);
        tripleStore.defineQueryPrefix("eu", CgmesNamespace.EU_NAMESPACE);
        queryCatalog = queryCatalogFor(cimVersion);
        Objects.requireNonNull(queryCatalog);
    }

    @Override
    public void read(InputStream is, String baseName, String contextName) {
        // Reset cached nodeBreaker value everytime we read new data
        nodeBreaker = null;
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

    @Override
    public void write(DataSource ds, CgmesSubset subset) {
        try {
            tripleStore.write(ds, contextNameFor(subset));
        } catch (TripleStoreException e) {
            throw new CgmesModelException(String.format("Writing. Triple store problem %s", ds), e);
        }
    }

    // Queries

    private static boolean isEquipmentCore(String profile) {
        return profile.contains("/EquipmentCore/") || profile.contains("/CIM/CoreEquipment");
    }

    private static boolean isEquipmentOperation(String profile) {
        return profile.contains("/EquipmentOperation/") || profile.contains("/CIM/Operation");
    }

    @Override
    public boolean hasEquipmentCore() {
        if (queryCatalog.containsKey(MODEL_PROFILES)) {
            PropertyBags r = namedQuery(MODEL_PROFILES);
            if (r == null) {
                return false;
            }
            for (PropertyBag m : r) {
                String p = m.get(PROFILE);
                if (p != null && isEquipmentCore(p)) {
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
        if (nodeBreaker == null) {
            nodeBreaker = computeIsNodeBreaker();
        }
        return nodeBreaker;
    }

    private boolean computeIsNodeBreaker() {
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
        Map<String, Boolean> modelHasOperationProfile = computeEqModelHasEquipmentOperationProfile(r);
        boolean consideredNodeBreaker = modelHasOperationProfile.values().stream().allMatch(Boolean::valueOf);
        if (LOG.isInfoEnabled()) {
            logNodeBreaker(consideredNodeBreaker, modelHasOperationProfile);
        }
        return consideredNodeBreaker;
    }

    private void logNodeBreaker(boolean consideredNodeBreaker, Map<String, Boolean> modelHasOperationProfile) {
        if (consideredNodeBreaker) {
            LOG.info(
                    "All FullModel objects have EquipmentOperation profile, so conversion will be considered node-breaker");
        } else {
            LOG.info(
                    "Following FullModel objects do not have EquipmentOperation profile, so conversion will not be considered node-breaker:");
            modelHasOperationProfile.entrySet().forEach(meqop -> {
                if (!meqop.getValue()) {
                    LOG.info("    {}", meqop.getKey());
                }
            });
        }
    }

    private Map<String, Boolean> computeEqModelHasEquipmentOperationProfile(PropertyBags modelProfiles) {
        Map<String, Boolean> modelHasOperationProfile = new HashMap<>();
        for (PropertyBag mp : modelProfiles) {
            String m = mp.get("FullModel");
            String p = mp.get(PROFILE);
            if (p != null) {
                if (isEquipmentCore(p) || p.contains("/EquipmentBoundary/")) {
                    modelHasOperationProfile.putIfAbsent(m, false);
                }
                if (isEquipmentOperation(p) || p.contains("/EquipmentBoundaryOperation/")) {
                    modelHasOperationProfile.put(m, true);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Model {} is considered node-breaker", m);
                    }
                }
            }
        }
        return modelHasOperationProfile;
    }

    @Override
    public PropertyBags fullModel(String cgmesProfile) {
        return namedQuery("fullModel", cgmesProfile);
    }

    @Override
    public PropertyBags fullModels() {
        return namedQuery("fullModel");
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
                    // Assume date time given as UTC if no explicit zone is specified
                    try {
                        d = DateTime.parse(s, ISODateTimeFormat.dateTimeParser().withOffsetParsed().withZoneUTC());
                    } catch (IllegalArgumentException e) {
                        LOG.error("Invalid date: {}. The date has been fixed to {}.", s, defaultValue);
                        return defaultValue;
                    }
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
        if (cachedNodes) {
            return cachedConnectivityNodes;
        }
        return namedQuery("connectivityNodes");
    }

    @Override
    public PropertyBags topologicalNodes() {
        if (cachedNodes) {
            return cachedTopologicalNodes;
        }
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
    public PropertyBags equivalentShunts() {
        return namedQuery("equivalentShunts");
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
    public PropertyBags phaseTapChangerTablesPoints() {
        return namedQuery("phaseTapChangerTablesPoints");
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
    public PropertyBags controlAreas() {
        return namedQuery("controlAreas");
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
    public PropertyBags tieFlows() {
        return namedQuery("tieFlows");
    }

    @Override
    public PropertyBags topologicalIslands() {
        return namedQuery("topologicalIslands");
    }

    @Override
    public PropertyBags graph() {
        return namedQuery("graph");
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

    public void namedQueryUpdate(String name, String... params) {
        String queryText = queryCatalog.get(name);
        if (queryText == null) {
            LOG.warn("Query [{}] not found in catalog", name);
        }
        queryText = injectParams(queryText, params);
        update(queryText);
    }

    public String getCimNamespace() {
        return cimNamespace;
    }

    public int getCimVersion() {
        return cimVersion;
    }

    public PropertyBags query(String queryText) {
        return tripleStore.query(queryText);
    }

    public void update(String queryText) {
        tripleStore.update(queryText);
    }

    @Override
    public TripleStore tripleStore() {
        return tripleStore;
    }

    // Updates

    public void update(
        String queryName,
        String context,
        String baseName,
        String subject,
        String predicate,
        String value,
        boolean valueIsUri) {
        Objects.requireNonNull(cimNamespace);
        String baseUri = getBaseUri(baseName);
        String value1 = valueIsUri ? baseUri.concat(value) : value;
        if (value.contains("cim:")) {
            value1 = cimNamespace.concat(value.substring(4));
        }
        namedQueryUpdate(
            queryName,
            context,
            baseUri.concat(subject),
            predicate,
            value1,
            String.valueOf(valueIsUri));
    }

    private String getBaseUri(String baseName) {
        if (tripleStore.getImplementationName().equals("rdf4j")) {
            return baseName.concat("/#");
        } else {
            return baseName.concat("#");
        }
    }

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

    @Override
    public void add(String context, String type, PropertyBags objects) {
        String contextName = EnumUtils.isValidEnum(CgmesSubset.class, context)
            ? contextNameFor(CgmesSubset.valueOf(context))
            : context;
        try {
            if (type.equals(CgmesNames.FULL_MODEL)) {
                tripleStore.add(contextName, mdNamespace(), type, objects);
            } else {
                tripleStore.add(contextName, cimNamespace, type, objects);
            }
        } catch (TripleStoreException x) {
            String msg = String.format("Adding objects of type %s to context %s", type, context);
            throw new CgmesModelException(msg, x);
        }
    }

    private String mdNamespace() {
        // Return the first namespace for the prefix md
        // If no namespace is found, return default
        PrefixNamespace def = new PrefixNamespace("md", CgmesNamespace.MD_NAMESPACE);
        return tripleStore.getNamespaces().stream().filter(ns -> ns.getPrefix().equals("md"))
            .findFirst().orElse(def).getNamespace();
    }

    private static final Pattern CIM_NAMESPACE_VERSION_PATTERN_UNTIL_16 = Pattern.compile("^.*CIM-schema-cim([0-9]+)#$");
    private static final Pattern CIM_NAMESPACE_VERSION_PATTERN_FROM_100 = Pattern.compile("^.*/CIM([0-9]+)#$");

    private static int cimVersionFromCimNamespace(String cimNamespace) {
        Matcher m = CIM_NAMESPACE_VERSION_PATTERN_UNTIL_16.matcher(cimNamespace);
        if (m.matches()) {
            return Integer.valueOf(m.group(1));
        } else {
            m = CIM_NAMESPACE_VERSION_PATTERN_FROM_100.matcher(cimNamespace);
            if (m.matches()) {
                return Integer.valueOf(m.group(1));
            }
        }
        return -1;
    }

    private String contextNameFor(CgmesSubset subset) {
        for (String context : tripleStore.contextNames()) {
            if (subset.isValidName(context)) {
                return context;
            }
        }
        return modelId() + "_" + subset + ".xml";
    }

    private QueryCatalog queryCatalogFor(int cimVersion) {
        QueryCatalog qc = null;
        String resourceName = null;
        if (cimVersion > 0) {
            resourceName = String.format("CIM%d.sparql", cimVersion);
        }
        if (resourceName != null) {
            qc = new QueryCatalog(resourceName);
        }
        return qc;
    }

    private String injectParams(String queryText, String... params) {
        String injected = queryText;
        // Avoid computing parameter reference for first parameters
        int k = 0;
        for (; k < Math.min(PARAMETER_REFERENCE.length, params.length); k++) {
            injected = injected.replace(PARAMETER_REFERENCE[k], params[k]);
        }
        for (; k < params.length; k++) {
            String paramRef = "{" + k + "}";
            injected = injected.replace(paramRef, params[k]);
        }
        return injected;
    }

    private final String cimNamespace;
    private final int cimVersion;
    private final TripleStore tripleStore;
    private final QueryCatalog queryCatalog;
    private Boolean nodeBreaker = null;

    private static final String MODEL_PROFILES = "modelProfiles";
    private static final String PROFILE = "profile";
    private static final Logger LOG = LoggerFactory.getLogger(CgmesModelTripleStore.class);
    private static final String[] PARAMETER_REFERENCE = {"{0}", "{1}", "{2}", "{3}", "{4}", "{5}", "{6}", "{7}", "{8}", "{9}"};
}
