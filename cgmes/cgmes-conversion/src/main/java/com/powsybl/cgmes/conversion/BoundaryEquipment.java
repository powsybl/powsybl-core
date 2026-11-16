/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.elements.ACLineSegmentConversion;
import com.powsybl.cgmes.conversion.elements.EquipmentAtBoundaryConversion;
import com.powsybl.cgmes.conversion.elements.EquivalentBranchConversion;
import com.powsybl.cgmes.conversion.elements.SwitchConversion;
import com.powsybl.cgmes.conversion.elements.transformers.TwoWindingsTransformerConversion;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class BoundaryEquipment {

    enum BoundaryEquipmentType {
        AC_LINE_SEGMENT, SWITCH, TRANSFORMER, EQUIVALENT_BRANCH
    }

    BoundaryEquipment(BoundaryEquipmentType type, PropertyBag propertyBag) {
        this.type = type;
        propertyBags.add(propertyBag);
    }

    BoundaryEquipment(BoundaryEquipmentType type, PropertyBags propertyBags) {
        this.type = type;
        this.propertyBags.addAll(propertyBags);
    }

    EquipmentAtBoundaryConversion createConversion(Context context) {
        return switch (type) {
            case AC_LINE_SEGMENT -> new ACLineSegmentConversion(propertyBags.get(0), context);
            case SWITCH -> new SwitchConversion(propertyBags.get(0), context);
            case TRANSFORMER -> new TwoWindingsTransformerConversion(propertyBags, context);
            case EQUIVALENT_BRANCH -> new EquivalentBranchConversion(propertyBags.get(0), context);
        };
    }

    boolean isAcLineSegmentDisconnected(Context context) {
        if (type == BoundaryEquipmentType.AC_LINE_SEGMENT) {
            return !(new ACLineSegmentConversion(propertyBags.get(0), context)).isConnectedAtBothEnds();
        }
        return false;
    }

    String getAcLineSegmentId() {
        if (type == BoundaryEquipmentType.AC_LINE_SEGMENT) {
            return propertyBags.get(0).getId("ACLineSegment");
        }
        return null;
    }

    void log() {
        if (LOG.isTraceEnabled()) {
            String title = "BoundaryEquipment " + type.toString();
            if (propertyBags.size() == 1) {
                LOG.trace(propertyBags.get(0).tabulateLocals(title));
            } else {
                propertyBags.forEach(p -> LOG.trace(p.tabulateLocals(title)));
            }
        }
    }

    private final BoundaryEquipmentType type;
    private final PropertyBags propertyBags = new PropertyBags();

    private static final Logger LOG = LoggerFactory.getLogger(BoundaryEquipment.class);
}
