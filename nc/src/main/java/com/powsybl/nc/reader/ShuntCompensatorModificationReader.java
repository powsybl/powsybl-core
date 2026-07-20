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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.nc.NcProfile;
import com.powsybl.nc.QueryManager;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.powsybl.nc.NcConverter.LOGGER;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class ShuntCompensatorModificationReader extends AbstractReader<ShuntCompensatorPositionAction> {
    private static final String SHUNT_COMPENSATOR_MODIFICATION_QUERY_NAME = "shuntCompensatorModification";
    private static final String STATIC_PROPERTY_RANGE_QUERY_NAME = "staticPropertyRange";
    private static final String PROPERTY_REFERENCE = "http://energy.referencedata.eu/PropertyReference/ShuntCompensator.sections";
    private static final String RELATIVE_DIRECTION_KIND = "http://entsoe.eu/ns/nc#RelativeDirectionKind.none";
    private static final String VALUE_OFFSET_KIND = "http://entsoe.eu/ns/nc#ValueOffsetKind.absolute";

    public ShuntCompensatorModificationReader(QueryManager queryManager, Network network) {
        super(queryManager, network);
    }

    public Set<ShuntCompensatorPositionAction> readFromProfiles() {
        Set<ShuntCompensatorPositionAction> shuntCompensatorPositionActions = new HashSet<>();
        PropertyBags shuntCompensatorModifications = queryManager.query(SHUNT_COMPENSATOR_MODIFICATION_QUERY_NAME, NcProfile.REMEDIAL_ACTION);
        PropertyBags staticPropertyRanges = queryManager.query(STATIC_PROPERTY_RANGE_QUERY_NAME, NcProfile.REMEDIAL_ACTION);
        Map<String, Set<PropertyBag>> staticPropertyRangesPerShuntCompensatorPositionAction = groupStaticPropertyRangesPerShuntCompensatorModification(staticPropertyRanges);
        for (PropertyBag shuntCompensatorModification : shuntCompensatorModifications) {
            Optional<ShuntCompensatorPositionAction> shuntCompensatorPositionAction = processShuntCompensatorModification(
                shuntCompensatorModification,
                staticPropertyRangesPerShuntCompensatorPositionAction.getOrDefault(shuntCompensatorModification.get("mRID"), Set.of()));
            shuntCompensatorPositionAction.ifPresent(shuntCompensatorPositionActions::add);
        }
        return shuntCompensatorPositionActions;
    }

    private static Map<String, Set<PropertyBag>> groupStaticPropertyRangesPerShuntCompensatorModification(PropertyBags staticPropertyRanges) {
        Map<String, Set<PropertyBag>> staticPropertyRangesPerShuntCompensatorModification = new HashMap<>();
        staticPropertyRanges.forEach(
            staticPropertyRange -> staticPropertyRangesPerShuntCompensatorModification.computeIfAbsent(ReaderUtils.getElementIdFromResourceUri(staticPropertyRange.get("gridStateAlteration")),
                k -> new HashSet<>()).add(staticPropertyRange));
        return staticPropertyRangesPerShuntCompensatorModification;
    }

    private Optional<ShuntCompensatorPositionAction> processShuntCompensatorModification(PropertyBag shuntCompensatorModification, Set<PropertyBag> staticPropertyRanges) {
        String shuntCompensatorPositionActionId = shuntCompensatorModification.get("mRID");
        String shuntCompensatorId = ReaderUtils.getElementIdFromResourceUri(shuntCompensatorModification.get("shuntCompensator"));
        ShuntCompensator shuntCompensatorInNetwork = network.getShuntCompensator(shuntCompensatorId);
        if (shuntCompensatorInNetwork == null) {
            LOGGER.warn("ShuntCompensatorModification {} refers to a non-existing shunt compensator {} and will be ignored.",
                shuntCompensatorPositionActionId, shuntCompensatorId);
            return Optional.empty();
        }
        String propertyReference = shuntCompensatorModification.get("propertyReference");
        if (!PROPERTY_REFERENCE.equals(propertyReference)) {
            LOGGER.warn("ShuntCompensatorModification {} has an invalid property reference and will be ignored (expected {}, got {}).",
                shuntCompensatorPositionActionId, PROPERTY_REFERENCE, propertyReference);
            return Optional.empty();
        }
        boolean normalEnabled = Boolean.parseBoolean(shuntCompensatorModification.getOrDefault("normalEnabled", "true"));
        if (!normalEnabled) {
            LOGGER.warn("ShuntCompensatorModification {} is not enabled and will be ignored.", shuntCompensatorPositionActionId);
            return Optional.empty();
        }
        // TODO: see how to handle following fields for OperatorStrategies
        String gridStateAlterationRemedialAction = shuntCompensatorModification.get("gridStateAlterationRemedialAction");
        String gridStateAlterationCollection = shuntCompensatorModification.get("gridStateAlterationCollection");
        return getSectionCount(staticPropertyRanges, shuntCompensatorPositionActionId)
            .map(sectionCount -> new ShuntCompensatorPositionActionBuilder()
                .withId(shuntCompensatorPositionActionId)
                .withShuntCompensatorId(shuntCompensatorId)
                .withSectionCount(sectionCount)
                .build());
    }

    private static Optional<Integer> getSectionCount(Set<PropertyBag> staticPropertyRanges, String shuntCompensatorPositionActionId) {
        if (staticPropertyRanges.isEmpty()) {
            LOGGER.warn("ShuntCompensatorModification {} has no static property range and will be ignored.", shuntCompensatorPositionActionId);
            return Optional.empty();
        } else if (staticPropertyRanges.size() > 1) {
            LOGGER.warn("ShuntCompensatorModification {} has multiple static property ranges and will be ignored.", shuntCompensatorPositionActionId);
            return Optional.empty();
        } else {
            PropertyBag staticPropertyRange = staticPropertyRanges.iterator().next();
            if (!PROPERTY_REFERENCE.equals(staticPropertyRange.get("propertyReference"))) {
                LOGGER.warn("StaticPropertyRange {} associated to ShuntCompensatorModification {} has an invalid property reference and will be ignored (expected {}, got {}).",
                    staticPropertyRange.get("mRID"), shuntCompensatorPositionActionId, PROPERTY_REFERENCE, staticPropertyRange.get("propertyReference"));
                return Optional.empty();
            }
            if (!RELATIVE_DIRECTION_KIND.equals(staticPropertyRange.get("direction"))) {
                LOGGER.warn("StaticPropertyRange {} associated to ShuntCompensatorModification {} has an invalid relative direction kind and will be ignored (expected {}, got {}).",
                    staticPropertyRange.get("mRID"), shuntCompensatorPositionActionId, RELATIVE_DIRECTION_KIND, staticPropertyRange.get("direction"));
                return Optional.empty();
            } else if (!VALUE_OFFSET_KIND.equals(staticPropertyRange.get("valueKind"))) {
                LOGGER.warn("StaticPropertyRange {} associated to ShuntCompensatorModification {} has an invalid value offset kind and will be ignored (expected {}, got {}).",
                    staticPropertyRange.get("mRID"), shuntCompensatorPositionActionId, VALUE_OFFSET_KIND, staticPropertyRange.get("valueKind"));
                return Optional.empty();
            } else {
                String normalValue = staticPropertyRange.get("normalValue");
                try {
                    int sectionCount = Integer.parseInt(normalValue);
                    if (sectionCount < 0) {
                        LOGGER.warn("StaticPropertyRange {} associated to ShuntCompensatorModification {} has an invalid normal value and will be ignored (expected positive integer, got {}).",
                            staticPropertyRange.get("mRID"), shuntCompensatorPositionActionId, normalValue);
                        return Optional.empty();
                    }
                    return Optional.of(sectionCount);
                } catch (NumberFormatException e) {
                    LOGGER.warn("StaticPropertyRange {} associated to ShuntCompensatorModification {} has an invalid normal value and will be ignored (expected positive integer, got {}).",
                        staticPropertyRange.get("mRID"), shuntCompensatorPositionActionId, normalValue);
                    return Optional.empty();
                }
            }
        }
    }
}
