/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseZone;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class ZoneData extends AbstractDataBlock<PsseZone> {

    private static final String[] FIELD_NAMES_35 = {"izone", "zoname"};
    private static final String[] FIELD_NAMES_33 = {"i", "zoname"};

    ZoneData() {
        super(PsseDataBlock.ZONE_DATA);
    }

    @Override
    public Class<PsseZone> psseTypeClass(PsseVersion version) {
        return PsseZone.class;
    }

    @Override
    public String[] fieldNames(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return FIELD_NAMES_35;
        } else {
            return FIELD_NAMES_33;
        }
    }
}
