/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import java.util.List;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseOwner;
import com.powsybl.psse.model.PsseRawModel;
import com.powsybl.psse.model.PsseVersion;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class OwnerData extends AbstractRecordGroup<PsseOwner> {

    private static final String[] FIELD_NAMES_35 = {"iowner", "owname"};
    private static final String[] FIELD_NAMES_33 = {"i", "owname"};
    private static final String[] QUOTED_FIELDS = {"owname"};

    OwnerData() {
        super(PsseRecordGroup.OWNER_DATA);
    }

    @Override
    public String[] fieldNames(PsseVersion version) {
        switch (version) {
            case VERSION_35:
                return FIELD_NAMES_35;
            case VERSION_33:
                return FIELD_NAMES_33;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    @Override
    public String[] quotedFields(PsseVersion version) {
        switch (version) {
            case VERSION_35:
            case VERSION_33:
                return QUOTED_FIELDS;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    @Override
    public Class<PsseOwner> psseTypeClass() {
        return PsseOwner.class;
    }

    @Override
    public List<PsseOwner> psseModelRecords(PsseRawModel model) {
        return model.getOwners();
    }

    @Override
    public String endOfBlockComment(PsseVersion version) {
        switch (version) {
            case VERSION_35:
            case VERSION_33:
                return "END OF OWNER DATA, BEGIN FACTS CONTROL DEVICE DATA";
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }
}
