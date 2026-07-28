/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.reader;

import com.powsybl.action.ShuntCompensatorPositionAction;
import com.powsybl.action.ShuntCompensatorPositionActionBuilder;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.nc.ImporterContext;
import com.powsybl.nc.QueryManager;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

import static com.powsybl.nc.NcConverter.LOGGER;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class ShuntCompensatorModificationReader extends AbstractActionReader<ShuntCompensatorPositionAction> {
    private static final String SHUNT_COMPENSATOR_MODIFICATION = "ShuntCompensatorModification";
    private static final String SHUNT_COMPENSATOR_MODIFICATION_QUERY_NAME = "shuntCompensatorModification";
    private static final String SHUNT_COMPENSATOR = "shuntCompensator";
    private static final String PROPERTY_REFERENCE = "http://energy.referencedata.eu/PropertyReference/ShuntCompensator.sections";

    public ShuntCompensatorModificationReader(QueryManager queryManager, ImporterContext importerContext, Network network) {
        super(queryManager, importerContext, network, SHUNT_COMPENSATOR_MODIFICATION, PROPERTY_REFERENCE,
            SHUNT_COMPENSATOR_MODIFICATION_QUERY_NAME, SHUNT_COMPENSATOR, false, ShuntCompensator.class);
    }

    @Override
    protected Optional<ShuntCompensatorPositionAction> convertGridStateAlterationToAction(String actionId,
                                                                                          Identifiable<?> networkElement,
                                                                                          PropertyBag gridStateAlteration,
                                                                                          PropertyBag staticPropertyRange,
                                                                                          VariationType variationType) {
        return getSectionCount(staticPropertyRange, actionId)
            .map(sectionCount -> new ShuntCompensatorPositionActionBuilder()
                .withId(actionId)
                .withShuntCompensatorId(networkElement.getId())
                .withSectionCount(sectionCount)
                .build());
    }

    private static Optional<Integer> getSectionCount(PropertyBag staticPropertyRange, String shuntCompensatorPositionActionId) {
        String normalValue = staticPropertyRange.get("normalValue");
        try {
            int sectionCount = Integer.parseInt(normalValue);
            if (sectionCount < 0) {
                throw new NumberFormatException("Negative section count: " + normalValue);
            }
            return Optional.of(sectionCount);
        } catch (NumberFormatException e) {
            LOGGER.warn("StaticPropertyRange {} associated to ShuntCompensatorModification {} has an invalid normal value and will be ignored (expected positive integer, got {}).",
                staticPropertyRange.get("mRID"), shuntCompensatorPositionActionId, normalValue);
            return Optional.empty();
        }
    }
}
