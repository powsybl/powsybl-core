/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseOwner;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class OwnerData extends AbstractRecordGroup<PsseOwner> {

    private static final String[] FIELD_NAMES_35 = {"iowner", "owname"};
    private static final String[] FIELD_NAMES_33 = {"i", "owname"};

    OwnerData() {
        super(PsseRecordGroup.OWNER_DATA);
    }

    @Override
    public Class<PsseOwner> psseTypeClass(PsseVersion version) {
        return PsseOwner.class;
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
