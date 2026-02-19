/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyMemDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class ReferenceDataProvider {
    private String sourcingActorName; // may be null, may be determined from country
    private final String countryName; // may be null, IIDM Country::name (iso alpha-2 code)
    private final ReadOnlyDataSource referenceDataSource; // may be null
    private final CgmesImport cgmesImport;
    private final Properties params; // may be null
    private PropertyBag sourcingActor;
    private CgmesModel referenceData = null;
    private final Map<Double, String> baseVoltagesByNominalVoltage = new HashMap<>();
    private String equipmentBoundaryId = null;
    private String topologyBoundaryId = null;
    private PropertyBags boundaryNodes = null;

    private boolean loaded = false;

    public ReferenceDataProvider(String sourcingActorName, String countryName, CgmesImport cgmesImport, Properties params) {
        this(sourcingActorName, countryName, null, cgmesImport, params);
    }

    public ReferenceDataProvider(String sourcingActorName, String countryName, ReadOnlyDataSource referenceDataSource, CgmesImport cgmesImport, Properties params) {
        Objects.requireNonNull(cgmesImport);
        this.sourcingActorName = sourcingActorName;
        this.countryName = countryName;
        this.referenceDataSource = referenceDataSource;
        this.cgmesImport = cgmesImport;
        this.params = params;
    }

    public String getBaseVoltage(double nominalV) {
        ensureReferenceDataIsLoaded();
        return baseVoltagesByNominalVoltage.get(nominalV);
    }

    public PropertyBags getBoundaryNodes() {
        return boundaryNodes;
    }

    public PropertyBag getSourcingActor() {
        ensureReferenceDataIsLoaded();
        return sourcingActor;
    }

    public Pair<String, String> getSourcingActorRegion() {
        ensureReferenceDataIsLoaded();
        if (sourcingActor.containsKey("GeographicalRegion") && sourcingActor.containsKey("geographicalRegionName")) {
            return Pair.of(sourcingActor.getId("GeographicalRegion"), sourcingActor.getLocal("geographicalRegionName"));
        }
        return null;
    }

    public String getEquipmentBoundaryId() {
        ensureReferenceDataIsLoaded();
        return equipmentBoundaryId;
    }

    public String getTopologyBoundaryId() {
        ensureReferenceDataIsLoaded();
        return topologyBoundaryId;
    }

    private void ensureReferenceDataIsLoaded() {
        if (loaded) {
            return;
        }
        loadReferenceData();
        loadBoundaryModelIds();
        loadBaseVoltages();
        loadSourcingActor();
        loadBoundaryNodes();
        loaded = true;
    }

    private void loadReferenceData() {
        // If no explicit data source is given,
        // force the load of boundaries configured in the platform or received as parameter using an empty data source
        loadReferenceData(Objects.requireNonNullElseGet(referenceDataSource, ReadOnlyMemDataSource::new));
    }

    private void loadReferenceData(ReadOnlyDataSource ds) {
        try {
            referenceData = cgmesImport.readCgmes(ds, params, ReportNode.NO_OP);
        } catch (CgmesModelException x) {
            // We have made an attempt to load it and discovered it is invalid
            referenceData = null;
        }
    }

    private void loadBoundaryModelIds() {
        if (referenceData == null) {
            return;
        }
        if (!(referenceData instanceof CgmesModelTripleStore referenceDataTs)) {
            return;
        }
        PropertyBags boundaryModelIds = referenceDataTs.namedQuery("boundaryModelIds");
        for (PropertyBag mid : boundaryModelIds) {
            String modelId = mid.getId("FullModel");
            String profile = mid.getLocal("profile");

            if (profile.contains("EquipmentBoundary")) {
                equipmentBoundaryId = modelId;
            } else if (profile.contains("TopologyBoundary")) {
                topologyBoundaryId = modelId;
            }
        }
    }

    private void loadBaseVoltages() {
        if (referenceData == null) {
            return;
        }
        baseVoltagesByNominalVoltage.clear();
        baseVoltagesByNominalVoltage.putAll(referenceData.baseVoltages().stream().collect(Collectors.toMap(
                bv -> bv.asDouble("nominalVoltage"),
                bv -> bv.getId("BaseVoltage")
        )));
    }

    private void loadBoundaryNodes() {
        if (referenceData == null) {
            return;
        }
        boundaryNodes = referenceData.boundaryNodes();
    }

    private void loadSourcingActor() {
        if (referenceData != null) {
            if (sourcingActorName == null || sourcingActorName.isEmpty()) {
                determineSourcingActorFromCountryName();
            }
            if (sourcingActorName != null && !sourcingActorName.isEmpty()) {
                PropertyBags sourcingActorRecords = referenceData.sourcingActor(sourcingActorName);
                if (sourcingActorRecords.size() > 1 && LOG.isWarnEnabled()) {
                    LOG.warn("Multiple records found for sourcing actor {}. Will consider only first one", sourcingActorName);
                    LOG.warn(sourcingActorRecords.tabulateLocals());
                } else if (sourcingActorRecords.isEmpty()) {
                    LOG.warn("Sourcing actor {} not found", sourcingActorName);
                } else {
                    sourcingActor = sourcingActorRecords.get(0);
                }
            }
        }
        if (sourcingActor == null) {
            sourcingActor = new PropertyBag(Collections.emptyList(), true);
        }
    }

    private void determineSourcingActorFromCountryName() {
        if (referenceData != null && countryName != null && !countryName.isEmpty()) {
            PropertyBags countryRecords = referenceData.countrySourcingActors(countryName);
            if (countryRecords.size() > 1 && LOG.isWarnEnabled()) {
                LOG.warn("Multiple sourcing actors found for country {}. Cannot determine a single sourcing actor", countryName);
                LOG.warn(countryRecords.tabulateLocals());
            } else if (countryRecords.isEmpty()) {
                LOG.warn("No sourcing actors found for country name {}", countryName);
            } else {
                sourcingActorName = countryRecords.get(0).getLocal("sourcingActorName");
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ReferenceDataProvider.class);
}
