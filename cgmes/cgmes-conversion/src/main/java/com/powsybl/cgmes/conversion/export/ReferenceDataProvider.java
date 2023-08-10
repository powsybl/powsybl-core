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
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyMemDataSource;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ReferenceDataProvider {
    private final String sourcingActorName;
    private final CgmesImport cgmesImport;
    private final Properties params;
    private PropertyBag sourcingActor;
    private CgmesModel referenceData = null;
    private final Map<Double, String> baseVoltagesByNominalVoltage = new HashMap<>();

    // TODO(Luma) try to memoize referenceData, map of baseVoltages and sourcing actor data instead of use this flag?
    private boolean loaded = false;

    public ReferenceDataProvider(String sourcingActorName, CgmesImport cgmesImport, Properties params) {
        this.sourcingActorName = sourcingActorName;
        this.cgmesImport = cgmesImport;
        this.params = params;
    }

    public String getBaseVoltage(double nominalV) {
        ensureReferenceDataIsLoaded();
        return baseVoltagesByNominalVoltage.get(nominalV);
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

    private void ensureReferenceDataIsLoaded() {
        if (loaded) {
            return;
        }
        loadReferenceData();
        loadBaseVoltages();
        loadSourcingActor();
        loaded = true;
    }

    private void loadReferenceData() {
        // Force the load of boundaries using an empty data source
        ReadOnlyDataSource emptyDataSource = new ReadOnlyMemDataSource();
        try {
            referenceData = cgmesImport.readCgmes(emptyDataSource, params, Reporter.NO_OP);
        } catch (CgmesModelException x) {
            // We have made an attempt to load it and discovered it is invalid
            referenceData = null;
        }
    }

    private void loadBaseVoltages() {
        if (referenceData == null) {
            return;
        }
        baseVoltagesByNominalVoltage.clear();
        baseVoltagesByNominalVoltage.putAll(referenceData.baseVoltages().stream().collect(Collectors.toMap(
                bv -> bv.asDouble("nominalVoltage"),
                bv -> bv.getLocal("BaseVoltage")
        )));
    }

    private void loadSourcingActor() {
        if (referenceData != null && sourcingActorName != null && !sourcingActorName.isEmpty()) {
            PropertyBags sourcingActorRecords = referenceData.sourcingActor(sourcingActorName);
            if (sourcingActorRecords.size() > 1 && LOG.isWarnEnabled()) {
                LOG.warn("Multiple records found for sourcing actor {}. Will consider only first one", sourcingActorName);
                LOG.warn(sourcingActorRecords.tabulateLocals());
            }
            sourcingActor = sourcingActorRecords.get(0);
        } else {
            sourcingActor = new PropertyBag(Collections.emptyList(), true);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ReferenceDataProvider.class);
}
