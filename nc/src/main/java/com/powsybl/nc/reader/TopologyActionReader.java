/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.reader;

import com.powsybl.action.SwitchAction;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
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
public class TopologyActionReader extends AbstractReader<SwitchAction> {
    private static final String TOPOLOGY_ACTION_QUERY_NAME = "topologyAction";
    private static final String STATIC_PROPERTY_RANGE_QUERY_NAME = "staticPropertyRange";
    private static final String PROPERTY_REFERENCE = "http://energy.referencedata.eu/PropertyReference/Switch.open";
    private static final String RELATIVE_DIRECTION_KIND = "http://entsoe.eu/ns/nc#RelativeDirectionKind.none";
    private static final String VALUE_OFFSET_KIND = "http://entsoe.eu/ns/nc#ValueOffsetKind.absolute";

    public TopologyActionReader(QueryManager queryManager, Network network) {
        super(queryManager, network);
    }

    public Set<SwitchAction> readFromProfiles() {
        Set<SwitchAction> switchActions = new HashSet<>();
        PropertyBags topologyActions = queryManager.query(TOPOLOGY_ACTION_QUERY_NAME, NcProfile.REMEDIAL_ACTION);
        PropertyBags staticPropertyRanges = queryManager.query(STATIC_PROPERTY_RANGE_QUERY_NAME, NcProfile.REMEDIAL_ACTION);
        Map<String, Set<PropertyBag>> staticPropertyRangesPerTopologyAction = groupStaticPropertyRangesPerTopologyAction(staticPropertyRanges);
        for (PropertyBag topologyAction : topologyActions) {
            Optional<SwitchAction> switchAction = processTopologyAction(topologyAction, staticPropertyRangesPerTopologyAction.getOrDefault(topologyAction.get("mRID"), Set.of()));
            switchAction.ifPresent(switchActions::add);
        }
        return switchActions;
    }

    private static Map<String, Set<PropertyBag>> groupStaticPropertyRangesPerTopologyAction(PropertyBags staticPropertyRanges) {
        Map<String, Set<PropertyBag>> staticPropertyRangesPerTopologyAction = new HashMap<>();
        staticPropertyRanges.forEach(
            staticPropertyRange -> staticPropertyRangesPerTopologyAction.computeIfAbsent(ReaderUtils.getElementIdFromResourceUri(staticPropertyRange.get("gridStateAlteration")),
                k -> new HashSet<>()).add(staticPropertyRange));
        return staticPropertyRangesPerTopologyAction;
    }

    private Optional<SwitchAction> processTopologyAction(PropertyBag topologyAction, Set<PropertyBag> staticPropertyRanges) {
        String switchActionId = topologyAction.get("mRID");
        String switchId = ReaderUtils.getElementIdFromResourceUri(topologyAction.get("switch"));
        Switch switchInNetwork = network.getSwitch(switchId);
        if (switchInNetwork == null) {
            LOGGER.warn("TopologyAction {} refers to a non-existing switch {} and will be ignored.", switchActionId, switchId);
            return Optional.empty();
        }
        String propertyReference = topologyAction.get("propertyReference");
        if (!PROPERTY_REFERENCE.equals(propertyReference)) {
            LOGGER.warn("TopologyAction {} has an invalid property reference and will be ignored (expected {}, got {}).", switchActionId, PROPERTY_REFERENCE, propertyReference);
            return Optional.empty();
        }
        boolean normalEnabled = Boolean.parseBoolean(topologyAction.getOrDefault("normalEnabled", "true"));
        if (!normalEnabled) {
            LOGGER.warn("TopologyAction {} is not enabled and will be ignored.", switchActionId);
            return Optional.empty();
        }
        // TODO: see how to handle following fields for OperatorStrategies
        String gridStateAlterationRemedialAction = topologyAction.get("gridStateAlterationRemedialAction");
        String gridStateAlterationCollection = topologyAction.get("gridStateAlterationCollection");
        return getOpen(staticPropertyRanges, switchActionId)
            .map(open -> new SwitchAction(switchActionId, switchId, open));
    }

    private static Optional<Boolean> getOpen(Set<PropertyBag> staticPropertyRanges, String switchActionId) {
        if (staticPropertyRanges.isEmpty()) {
            LOGGER.warn("TopologyAction {} has no static property range and will be ignored.", switchActionId);
            return Optional.empty();
        } else if (staticPropertyRanges.size() > 1) {
            LOGGER.warn("TopologyAction {} has multiple static property ranges and will be ignored.", switchActionId);
            return Optional.empty();
        } else {
            PropertyBag staticPropertyRange = staticPropertyRanges.iterator().next();
            if (!PROPERTY_REFERENCE.equals(staticPropertyRange.get("propertyReference"))) {
                LOGGER.warn("StaticPropertyRange {} associated to TopologyAction {} has an invalid property reference and will be ignored (expected {}, got {}).",
                    staticPropertyRange.get("mRID"), switchActionId, PROPERTY_REFERENCE, staticPropertyRange.get("propertyReference"));
                return Optional.empty();
            }
            if (!RELATIVE_DIRECTION_KIND.equals(staticPropertyRange.get("direction"))) {
                LOGGER.warn("StaticPropertyRange {} associated to TopologyAction {} has an invalid relative direction kind and will be ignored (expected {}, got {}).",
                    staticPropertyRange.get("mRID"), switchActionId, RELATIVE_DIRECTION_KIND, staticPropertyRange.get("direction"));
                return Optional.empty();
            } else if (!VALUE_OFFSET_KIND.equals(staticPropertyRange.get("valueKind"))) {
                LOGGER.warn("StaticPropertyRange {} associated to TopologyAction {} has an invalid value offset kind and will be ignored (expected {}, got {}).", staticPropertyRange.get("mRID"),
                    switchActionId, VALUE_OFFSET_KIND, staticPropertyRange.get("valueKind"));
                return Optional.empty();
            } else {
                String normalValue = staticPropertyRange.get("normalValue");
                if ("0".equals(normalValue)) {
                    return Optional.of(false);
                } else if ("1".equals(normalValue)) {
                    return Optional.of(true);
                } else {
                    LOGGER.warn("StaticPropertyRange {} associated to TopologyAction {} has an invalid normal value and will be ignored (expected 0 or 1, got {}).",
                        staticPropertyRange.get("mRID"), switchActionId, normalValue);
                    return Optional.empty();
                }
            }
        }
    }
}
