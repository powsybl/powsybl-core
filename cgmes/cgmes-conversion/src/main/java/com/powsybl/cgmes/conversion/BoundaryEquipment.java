/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */

public class BoundaryEquipment {

    enum BoundaryEquipmentType {
        AC_LINE_SEGMENT, SWITCH, TRANSFORMER, EQUIVALENT_BRANCH
    }

    BoundaryEquipment(BoundaryEquipmentType type, PropertyBag propertyBag) {
        this.type = type;
        this.propertyBag = propertyBag;
        propertyBags = null;
    }

    BoundaryEquipment(BoundaryEquipmentType type, PropertyBags propertyBags) {
        this.type = type;
        this.propertyBag = null;
        this.propertyBags = propertyBags;
    }

    BoundaryEquipmentType getBoundaryEquipmentType() {
        return type;
    }

    EquipmentAtBoundaryConversion createConversion(Context context) {
        EquipmentAtBoundaryConversion c = null;
        switch (type) {
            case AC_LINE_SEGMENT:
                c = new ACLineSegmentConversion(propertyBag, context);
                break;
            case SWITCH:
                c = new SwitchConversion(propertyBag, context);
                break;
            case TRANSFORMER:
                c = new TwoWindingsTransformerConversion(propertyBags, context);
                break;
            case EQUIVALENT_BRANCH:
                c = new EquivalentBranchConversion(propertyBag, context);
                break;
        }
        return c;
    }

    void log() {
        if (LOG.isDebugEnabled()) {
            if (propertyBag != null) {
                LOG.debug(propertyBag.tabulateLocals(type.toString()));
            }
            if (propertyBags != null) {
                propertyBags.forEach(p -> LOG.debug(p.tabulateLocals(type.toString())));
            }
        }
    }

    private final BoundaryEquipmentType type;
    private final PropertyBag propertyBag;
    private final PropertyBags propertyBags;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesBoundary.class);
}
