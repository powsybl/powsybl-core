/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseSwitchedShunt;
import com.powsybl.psse.model.PsseSwitchedShunt35;
import com.powsybl.psse.model.PsseVersion;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class SwitchedShuntData extends AbstractRecordGroup<PsseSwitchedShunt> {

    private static final String[] FIELD_NAMES_35 = {"i", "id", "modsw", "adjm", "stat", "vswhi", "vswlo", "swreg", "nreg", "rmpct", "rmidnt", "binit",
        "s1", "n1", "b1", "s2", "n2", "b2", "s3", "n3", "b3", "s4", "n4", "b4", "s5", "n5", "b5", "s6", "n6", "b6",
        "s7", "n7", "b7", "s8", "n8", "b8"};
    private static final String[] FIELD_NAMES_33 = {"i", "modsw", "adjm", "stat", "vswhi", "vswlo", "swrem", "rmpct", "rmidnt", "binit",
        "n1", "b1", "n2", "b2", "n3", "b3", "n4", "b4", "n5", "b5", "n6", "b6", "n7", "b7", "n8", "b8"};

    SwitchedShuntData() {
        super(PsseRecordGroup.SWITCHED_SHUNT_DATA);
    }

    @Override
    public Class<? extends PsseSwitchedShunt> psseTypeClass(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return PsseSwitchedShunt35.class;
        } else {
            return PsseSwitchedShunt.class;
        }
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
