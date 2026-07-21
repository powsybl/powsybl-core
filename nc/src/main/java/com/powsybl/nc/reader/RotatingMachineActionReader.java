/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.reader;

import com.powsybl.action.GeneratorAction;
import com.powsybl.action.GeneratorActionBuilder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.NcProfile;
import com.powsybl.nc.QueryManager;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.powsybl.nc.NcConverter.LOGGER;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class RotatingMachineActionReader extends AbstractReader<GeneratorAction> {
    private static final String ROTATING_MACHINE_ACTION_QUERY_NAME = "rotatingMachineAction";
    private static final String STATIC_PROPERTY_RANGE_QUERY_NAME = "staticPropertyRange";
    private static final String PROPERTY_REFERENCE = "http://energy.referencedata.eu/PropertyReference/RotatingMachine.p";
    private static final String RELATIVE_DIRECTION_KIND_NONE = "http://entsoe.eu/ns/nc#RelativeDirectionKind.none";
    private static final String RELATIVE_DIRECTION_KIND_UP = "http://entsoe.eu/ns/nc#RelativeDirectionKind.up";
    private static final String RELATIVE_DIRECTION_KIND_DOWN = "http://entsoe.eu/ns/nc#RelativeDirectionKind.down";
    private static final String VALUE_OFFSET_KIND_ABSOLUTE = "http://entsoe.eu/ns/nc#ValueOffsetKind.absolute";
    private static final String VALUE_OFFSET_KIND_INCREMENTAL = "http://entsoe.eu/ns/nc#ValueOffsetKind.incremental";
    // TODO: handle incrementalPercentage?

    public RotatingMachineActionReader(QueryManager queryManager, Network network) {
        super(queryManager, network);
    }

    public Set<GeneratorAction> readFromProfiles() {
        Set<GeneratorAction> generatorActions = new HashSet<>();
        PropertyBags rotatingMachineActions = queryManager.query(ROTATING_MACHINE_ACTION_QUERY_NAME, NcProfile.REMEDIAL_ACTION);
        PropertyBags staticPropertyRanges = queryManager.query(STATIC_PROPERTY_RANGE_QUERY_NAME, NcProfile.REMEDIAL_ACTION);
        Map<String, Set<PropertyBag>> staticPropertyRangesPerGeneratorAction = groupStaticPropertyRangesPerGeneratorAction(staticPropertyRanges);
        for (PropertyBag rotatingMachineAction : rotatingMachineActions) {
            Optional<GeneratorAction> switchAction = processGeneratorAction(rotatingMachineAction, staticPropertyRangesPerGeneratorAction.getOrDefault(rotatingMachineAction.get("mRID"), Set.of()));
            switchAction.ifPresent(generatorActions::add);
        }
        return generatorActions;
    }

    private static Map<String, Set<PropertyBag>> groupStaticPropertyRangesPerGeneratorAction(PropertyBags staticPropertyRanges) {
        Map<String, Set<PropertyBag>> staticPropertyRangesPerGeneratorAction = new HashMap<>();
        staticPropertyRanges.forEach(
            staticPropertyRange -> staticPropertyRangesPerGeneratorAction.computeIfAbsent(ReaderUtils.getElementIdFromResourceUri(staticPropertyRange.get("gridStateAlteration")),
                k -> new HashSet<>()).add(staticPropertyRange));
        return staticPropertyRangesPerGeneratorAction;
    }

    private Optional<GeneratorAction> processGeneratorAction(PropertyBag generatorAction, Set<PropertyBag> staticPropertyRanges) {
        String generatorActionId = generatorAction.get("mRID");
        String generatorId = ReaderUtils.getElementIdFromResourceUri(generatorAction.get("rotatingMachine"));
        Generator generatorInNetwork = network.getGenerator(generatorId);
        if (generatorInNetwork == null) {
            LOGGER.warn("RotatingMachineAction {} refers to a non-existing generator {} and will be ignored.", generatorActionId, generatorId);
            return Optional.empty();
        }
        String propertyReference = generatorAction.get("propertyReference");
        if (!PROPERTY_REFERENCE.equals(propertyReference)) {
            LOGGER.warn("RotatingMachineAction {} has an invalid property reference and will be ignored (expected {}, got {}).", generatorActionId, PROPERTY_REFERENCE, propertyReference);
            return Optional.empty();
        }
        boolean normalEnabled = Boolean.parseBoolean(generatorAction.getOrDefault("normalEnabled", "true"));
        if (!normalEnabled) {
            LOGGER.warn("RotatingMachineAction {} is not enabled and will be ignored.", generatorActionId);
            return Optional.empty();
        }
        // TODO: see how to handle following fields for OperatorStrategies
        String gridStateAlterationRemedialAction = generatorAction.get("gridStateAlterationRemedialAction");
        String gridStateAlterationCollection = generatorAction.get("gridStateAlterationCollection");
        if (!checkOnlyOneStaticPropertyRange(staticPropertyRanges, generatorActionId)) {
            return Optional.empty();
        }
        return getSetPointAndRelative(staticPropertyRanges.iterator().next(), generatorActionId)
            .map(setPointAndRelative -> new GeneratorActionBuilder()
                .withId(generatorActionId)
                .withGeneratorId(generatorId)
                .withActivePowerRelativeValue(setPointAndRelative.getRight())
                .withActivePowerValue(setPointAndRelative.getLeft())
                .build());
    }

    private static Optional<Pair<Double, Boolean>> getSetPointAndRelative(PropertyBag staticPropertyRange, String generatorActionId) {
        String normalValue = staticPropertyRange.get("normalValue");
        double setPoint;
        try {
            setPoint = Double.parseDouble(normalValue);
        } catch (NumberFormatException e) {
            LOGGER.warn("StaticPropertyRange {} associated to RotatingMachineAction {} has an invalid normal value and will be ignored (expected integer, got {}).",
                staticPropertyRange.get("mRID"), generatorActionId, normalValue);
            return Optional.empty();
        }

        String direction = staticPropertyRange.get("direction");
        String valueKind = staticPropertyRange.get("valueKind");
        if (RELATIVE_DIRECTION_KIND_NONE.equals(direction) && VALUE_OFFSET_KIND_ABSOLUTE.equals(valueKind)) {
            return Optional.of(Pair.of(setPoint, false));
        } else if (RELATIVE_DIRECTION_KIND_UP.equals(direction) && VALUE_OFFSET_KIND_INCREMENTAL.equals(valueKind)) {
            return Optional.of(Pair.of(setPoint, true));
        } else if (RELATIVE_DIRECTION_KIND_DOWN.equals(direction) && VALUE_OFFSET_KIND_INCREMENTAL.equals(valueKind)) {
            return Optional.of(Pair.of(-setPoint, true));
        } // TODO: incrementalPercentage?
        LOGGER.warn("StaticPropertyRange {} associated to RotatingMachineAction {} has an invalid combination of relativeDirectionKind and valueOffsetKind.",
            staticPropertyRange.get("mRID"), generatorActionId);
        return Optional.empty();
    }

    private static boolean checkOnlyOneStaticPropertyRange(Set<PropertyBag> staticPropertyRanges, String generatorActionId) {
        if (staticPropertyRanges.isEmpty()) {
            LOGGER.warn("RotatingMachineAction {} has no static property range and will be ignored.", generatorActionId);
            return false;
        } else if (staticPropertyRanges.size() > 1) {
            LOGGER.warn("RotatingMachineAction {} has multiple static property ranges and will be ignored.", generatorActionId);
            return false;
        }
        PropertyBag staticPropertyRange = staticPropertyRanges.iterator().next();
        if (!PROPERTY_REFERENCE.equals(staticPropertyRange.get("propertyReference"))) {
            LOGGER.warn("StaticPropertyRange {} associated to RotatingMachineAction {} has an invalid property reference and will be ignored (expected {}, got {}).",
                staticPropertyRange.get("mRID"), generatorActionId, PROPERTY_REFERENCE, staticPropertyRange.get("propertyReference"));
            return false;
        }
        return true;
    }
}
