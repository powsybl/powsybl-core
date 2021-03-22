/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.reporter.MarkerImpl;
import com.powsybl.commons.reporter.Reporter;

import java.util.Map;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class ReporterNetworkListener implements NetworkListener {
    private final Reporter reporter;

    public ReporterNetworkListener(Reporter reporter) {
        this.reporter = reporter;
    }

    private static Map<String, Object> getValuesMap(Identifiable identifiable) {
        return Map.of("class", identifiable.getClass().getSimpleName(),
            "id", identifiable.getId(),
            "variant", identifiable.getNetwork().getVariantManager().getWorkingVariantId(),
            "network", identifiable.getNetwork());
    }

    @Override
    public void onCreation(Identifiable identifiable) {
        reporter.report("identifiableCreation_" + identifiable.getId(), "{class} {id} creation",
            getValuesMap(identifiable), MarkerImpl.TRACE);
    }

    @Override
    public void onRemoval(Identifiable identifiable) {
        reporter.report("identifiableRemoval_" + identifiable.getId(), "{class} {id} removed",
            getValuesMap(identifiable), MarkerImpl.TRACE);
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        reporter.report("identifiableUpdated_" + identifiable.getId(),
            "{class} {id} updated: {attribute} changed from {oldValue} to {newValue}",
            Map.of("class", identifiable.getClass().getSimpleName(), "id", identifiable.getId(),
                "variant", identifiable.getNetwork().getVariantManager().getWorkingVariantId(), "network", identifiable.getNetwork(),
                "attribute", attribute, "oldValue", oldValue, "newValue", newValue),
            MarkerImpl.TRACE);
    }

}
