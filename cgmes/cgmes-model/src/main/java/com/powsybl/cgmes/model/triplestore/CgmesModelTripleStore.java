/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.triplestore;

import java.io.InputStream;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.AbstractCgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.Subset;
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
        queryCatalog = queryCatalogFor(cimNamespace);
        Objects.requireNonNull(queryCatalog);
    }

    public void read(String base, String contextName, InputStream is) {
        tripleStore.read(base, contextName, is);
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
    public boolean isNodeBreaker() {
        // Optimization hint: consider caching the results of the query for model
        // profiles
        if (queryCatalog.containsKey("modelProfiles")) {
            PropertyBags r = namedQuery("modelProfiles");
            if (r == null) {
                return false;
            }
            for (PropertyBag m : r) {
                String p = m.get("profile");
                if (p != null && p.contains("/EquipmentOperation/")) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Model is considered node-breaker because {} has profile {}", m.get("FullModel"), p);
                    }
                    return true;
                }
            }
        }
        return false;
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
    public PropertyBags terminalsTP() {
        return namedQuery("terminalsTP");
    }

    @Override
    public PropertyBags terminalsCN() {
        return namedQuery("terminalsCN");
    }

    @Override
    public PropertyBags terminalLimits() {
        return namedQuery("terminalLimits");
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
    public PropertyBags asynchronousMachines() {
        return namedQuery("asynchronousMachines");
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

    public TripleStore tripleStore() {
        return tripleStore;
    }

    // Updates

    @Override
    public void clear(Subset subset) {
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
    public void add(String contextName, String type, PropertyBags objects) {
        try {
            tripleStore.add(contextName, cimNamespace + type, objects);
        } catch (TripleStoreException x) {
            String msg = String.format("Adding objects of type %s to context %s", type, contextName);
            throw new CgmesModelException(msg, x);
        }
    }

    // Private

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

    private static final Logger LOG = LoggerFactory.getLogger(CgmesModelTripleStore.class);
}
