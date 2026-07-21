/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.reader;

import com.powsybl.action.SwitchAction;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.nc.QueryManager;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.Optional;

import static com.powsybl.nc.NcConverter.LOGGER;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public class TopologyActionReader extends AbstractActionReader<SwitchAction> {
    private static final String TOPOLOGY_ACTION = "TopologyAction";
    private static final String TOPOLOGY_ACTION_QUERY_NAME = "topologyAction";
    private static final String SWITCH = "switch";
    private static final String PROPERTY_REFERENCE = "http://energy.referencedata.eu/PropertyReference/Switch.open";
    private static final String RELATIVE_DIRECTION_KIND = "http://entsoe.eu/ns/nc#RelativeDirectionKind.none";
    private static final String VALUE_OFFSET_KIND = "http://entsoe.eu/ns/nc#ValueOffsetKind.absolute";

    public TopologyActionReader(QueryManager queryManager, Network network) {
        super(queryManager, network, TOPOLOGY_ACTION, PROPERTY_REFERENCE, TOPOLOGY_ACTION_QUERY_NAME, SWITCH, Switch.class);
    }

    @Override
    protected Optional<SwitchAction> convertGridStateAlterationToAction(String actionId, Identifiable<?> networkElement, PropertyBag gridStateAlteration, PropertyBag staticPropertyRange) {
        return getOpen(staticPropertyRange, actionId)
            .map(open -> new SwitchAction(actionId, networkElement.getId(), open));
    }

    private static Optional<Boolean> getOpen(PropertyBag staticPropertyRange, String switchActionId) {
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
