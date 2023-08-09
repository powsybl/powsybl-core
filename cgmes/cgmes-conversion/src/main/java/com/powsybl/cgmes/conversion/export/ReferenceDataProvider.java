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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class ReferenceDataProvider {
    private final CgmesImport cgmesImport;
    private final Properties params;
    private CgmesModel referenceData = null;
    private final Map<Double, String> baseVoltagesByNominalVoltage = new HashMap<>();

    // FIXME(Luma) memoize referenceData and map of baseVoltages when they are first needed
    private boolean loaded = false;

    public ReferenceDataProvider(CgmesImport cgmesImport, Properties params) {
        this.cgmesImport = cgmesImport;
        this.params = params;
    }

    public String getBaseVoltage(double nominalV) {
        ensureReferenceDataIsLoaded();
        return baseVoltagesByNominalVoltage.get(nominalV);
    }

    public PropertyBag getSourcingActor(String sourcingActorName) {
        ensureReferenceDataIsLoaded();

        PropertyBags sourcingActorRecords = referenceData.sourcingActor(sourcingActorName);
        if (sourcingActorRecords.size() > 1 && LOG.isWarnEnabled()) {
            LOG.warn("Multiple records found for sourcing actor {}. Will consider only first one", sourcingActorName);
            LOG.warn(sourcingActorRecords.tabulateLocals());
        }
        return sourcingActorRecords.get(0);
    }

    private void ensureReferenceDataIsLoaded() {
        if (loaded) {
            return;
        }
        ReadOnlyDataSource emptyDataSource = new ReadOnlyMemDataSource();
        try {
            referenceData = cgmesImport.readCgmes(emptyDataSource, params, Reporter.NO_OP);
        } catch (CgmesModelException x) {
            // We have made an attempt to load it and discovered it is invalid
            loaded = true;
            return;
        }
        baseVoltagesByNominalVoltage.clear();
        baseVoltagesByNominalVoltage.putAll(referenceData.baseVoltages().stream().collect(Collectors.toMap(
                bv -> bv.asDouble("nominalVoltage"),
                bv -> bv.getLocal("BaseVoltage")
        )));
        loaded = true;
    }

    private static final Logger LOG = LoggerFactory.getLogger(ReferenceDataProvider.class);
}
