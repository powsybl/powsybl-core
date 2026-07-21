/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.reader;

import com.powsybl.action.Action;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.NcProfile;
import com.powsybl.nc.QueryManager;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.powsybl.nc.NcConverter.LOGGER;
import static com.powsybl.nc.reader.ReaderUtils.MRID;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public abstract class AbstractActionReader<A extends Action> extends AbstractReader<A> {
    private static final String STATIC_PROPERTY_RANGE_QUERY_NAME = "staticPropertyRange";
    private static final String RELATIVE_DIRECTION_KIND_NONE = "http://entsoe.eu/ns/nc#RelativeDirectionKind.none";
    private static final String RELATIVE_DIRECTION_KIND_UP = "http://entsoe.eu/ns/nc#RelativeDirectionKind.up";
    private static final String RELATIVE_DIRECTION_KIND_DOWN = "http://entsoe.eu/ns/nc#RelativeDirectionKind.down";
    private static final String VALUE_OFFSET_KIND_ABSOLUTE = "http://entsoe.eu/ns/nc#ValueOffsetKind.absolute";
    private static final String VALUE_OFFSET_KIND_INCREMENTAL = "http://entsoe.eu/ns/nc#ValueOffsetKind.incremental";

    protected final String queryName;
    protected final String gridStateAlterationType;
    protected final String propertyReference;
    protected final String networkElementAttribute;
    protected final boolean allowIncremental;
    protected final Class<? extends Identifiable<?>> identifiableClass;

    public AbstractActionReader(QueryManager queryManager,
                                Network network,
                                String gridStateAlterationType,
                                String propertyReference,
                                String queryName,
                                String networkElementAttribute,
                                boolean allowIncremental,
                                Class<? extends Identifiable<?>> identifiableClass) {
        super(queryManager, network);
        this.queryName = queryName;
        this.gridStateAlterationType = gridStateAlterationType;
        this.propertyReference = propertyReference;
        this.networkElementAttribute = networkElementAttribute;
        this.allowIncremental = allowIncremental;
        this.identifiableClass = identifiableClass;
    }

    public Set<A> readFromProfiles() {
        Set<A> actions = new HashSet<>();
        PropertyBags gridStateAlterations = queryManager.query(queryName, NcProfile.REMEDIAL_ACTION);
        PropertyBags staticPropertyRanges = queryManager.query(STATIC_PROPERTY_RANGE_QUERY_NAME, NcProfile.REMEDIAL_ACTION);
        Map<String, Set<PropertyBag>> staticPropertyRangesPerGridStateAlteration = ReaderUtils.groupOnAttribute(staticPropertyRanges, "gridStateAlteration", true);
        for (PropertyBag gridStateAlteration : gridStateAlterations) {
            Optional<A> action = processGridStateAlteration(gridStateAlteration, staticPropertyRangesPerGridStateAlteration.getOrDefault(gridStateAlteration.get(MRID), Set.of()));
            action.ifPresent(actions::add);
        }
        return actions;
    }

    private Optional<A> processGridStateAlteration(PropertyBag gridStateAlteration, Set<PropertyBag> staticPropertyRanges) {
        String actionId = gridStateAlteration.get(MRID);
        String networkElementId = ReaderUtils.getElementIdFromResourceUri(gridStateAlteration.get(networkElementAttribute));

        Identifiable<?> networkElement = network.getIdentifiable(networkElementId);
        if (!identifiableClass.isInstance(networkElement)) {
            LOGGER.warn("{} {} refers to a non-existing {} {} and will be ignored.",
                gridStateAlterationType, actionId, identifiableClass.getSimpleName(), networkElementId);
            return Optional.empty();
        }

        if (!propertyReference.equals(gridStateAlteration.get("propertyReference"))) {
            LOGGER.warn("{} {} has an invalid property reference and will be ignored (expected {}, got {}).",
                gridStateAlterationType, actionId, propertyReference, gridStateAlteration.get("propertyReference"));
            return Optional.empty();
        }

        boolean normalEnabled = Boolean.parseBoolean(gridStateAlteration.getOrDefault("normalEnabled", "true"));
        if (!normalEnabled) {
            LOGGER.warn("{} {} is not enabled and will be ignored.", gridStateAlterationType, actionId);
            return Optional.empty();
        }

        // TODO: see how to handle following fields for OperatorStrategies
        String gridStateAlterationRemedialAction = gridStateAlteration.get("gridStateAlterationRemedialAction");
        String gridStateAlterationCollection = gridStateAlteration.get("gridStateAlterationCollection");

        if (staticPropertyRanges.isEmpty()) {
            LOGGER.warn("{} {} has no static property range and will be ignored.", gridStateAlterationType, actionId);
            return Optional.empty();
        } else if (staticPropertyRanges.size() > 1) {
            LOGGER.warn("{} {} has multiple static property ranges and will be ignored.", gridStateAlterationType, actionId);
            return Optional.empty();
        }

        PropertyBag staticPropertyRange = staticPropertyRanges.iterator().next();
        if (!propertyReference.equals(staticPropertyRange.get("propertyReference"))) {
            LOGGER.warn("StaticPropertyRange {} associated to {} {} has an invalid property reference and will be ignored (expected {}, got {}).",
                staticPropertyRange.get(MRID), gridStateAlterationType, actionId, propertyReference, staticPropertyRange.get("propertyReference"));
            return Optional.empty();
        }

        String direction = staticPropertyRange.get("direction");
        String valueKind = staticPropertyRange.get("valueKind");

        VariationType variationType;
        if (RELATIVE_DIRECTION_KIND_NONE.equals(direction) && VALUE_OFFSET_KIND_ABSOLUTE.equals(valueKind)) {
            variationType = VariationType.ABSOLUTE;
        } else if (allowIncremental && RELATIVE_DIRECTION_KIND_UP.equals(direction) && VALUE_OFFSET_KIND_INCREMENTAL.equals(valueKind)) {
            variationType = VariationType.INCREMENTAL_UP;
        } else if (allowIncremental && RELATIVE_DIRECTION_KIND_DOWN.equals(direction) && VALUE_OFFSET_KIND_INCREMENTAL.equals(valueKind)) {
            variationType = VariationType.INCREMENTAL_DOWN;
        } else {
            LOGGER.warn("StaticPropertyRange {} associated to {} {} has an invalid relative direction kind and value offset kind combination and will be ignored.",
                staticPropertyRange.get(MRID), gridStateAlterationType, actionId);
            return Optional.empty();
        }

        return convertGridStateAlterationToAction(actionId, networkElement, gridStateAlteration, staticPropertyRange, variationType);
    }

    protected abstract Optional<A> convertGridStateAlterationToAction(String actionId,
                                                                      Identifiable<?> networkElement,
                                                                      PropertyBag gridStateAlteration,
                                                                      PropertyBag staticPropertyRange,
                                                                      VariationType variationType);

    protected enum VariationType {
        ABSOLUTE, INCREMENTAL_UP, INCREMENTAL_DOWN
    }
}
