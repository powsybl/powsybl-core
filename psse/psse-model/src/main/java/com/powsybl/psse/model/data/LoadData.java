/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseLoad;
import com.powsybl.psse.model.PsseVersion;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class LoadData extends AbstractRecordGroup<PsseLoad> {

    private static final String[] FIELD_NAMES_35 = {"ibus", "loadid", "stat", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale", "intrpt", "dgenp", "dgenq", "dgenm", "loadtype"};
    private static final String[] FIELD_NAMES_33 = {"i", "id", "status", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale", "intrpt"};
    private static final String[] QUOTED_FIELDS_35 = {"loadid", "loadtype"};
    private static final String[] QUOTED_FIELDS_33 = {"id"};

    LoadData() {
        super(PsseRecordGroup.LOAD_DATA);
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
                return QUOTED_FIELDS_35;
            case VERSION_33:
                return QUOTED_FIELDS_33;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    @Override
    public Class<PsseLoad> psseTypeClass() {
        return PsseLoad.class;
    }

    @Override
    public String endOfBlockComment(PsseVersion version) {
        switch (version) {
            case VERSION_35:
            case VERSION_33:
                return "END OF LOAD DATA, BEGIN FIXED SHUNT DATA";
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }
}
