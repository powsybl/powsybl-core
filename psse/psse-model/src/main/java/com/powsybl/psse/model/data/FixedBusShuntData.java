/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseConstants.PsseVersion;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseFixedShunt;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class FixedBusShuntData extends AbstractRecordGroup<PsseFixedShunt> {

    private static final String[] FIELD_NAMES_35 = {"ibus", "shntid", "stat", "gl", "bl"};
    private static final String[] FIELD_NAMES_33 = {"i", "id", "status", "gl", "bl"};

    public FixedBusShuntData() {
        super(PsseRecordGroup.FIXED_BUS_SHUNT_DATA);
    }

    @Override
    public Class<PsseFixedShunt> psseTypeClass(PsseVersion version) {
        return PsseFixedShunt.class;
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
}
