/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.PsseZone;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class ZoneData extends AbstractRecordGroup<PsseZone> {

    private static final String[] FIELD_NAMES_35 = {"izone", "zoname"};
    private static final String[] FIELD_NAMES_33 = {"i", "zoname"};
    private static final String[] QUOTED_FIELDS = {"zoname"};

    ZoneData() {
        super(PsseRecordGroup.ZONE);
    }

    @Override
    public String[] fieldNames(PsseVersion version) {
        switch (version.major()) {
            case V35:
                return FIELD_NAMES_35;
            case V33:
                return FIELD_NAMES_33;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    @Override
    public String[] quotedFields(PsseVersion version) {
        switch (version.major()) {
            case V35:
            case V33:
                return QUOTED_FIELDS;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    @Override
    public Class<PsseZone> psseTypeClass() {
        return PsseZone.class;
    }
}
