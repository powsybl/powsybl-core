/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseSwitchedShunt;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V32;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_RMIDNT;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SwitchedShuntData extends AbstractRecordGroup<PsseSwitchedShunt> {

    private static final String[] FIELD_NAMES_32_33 = {"i", "modsw", "adjm", "stat", "vswhi", "vswlo", "swrem", "rmpct", STR_RMIDNT, "binit", "n1", "b1", "n2", "b2", "n3", "b3", "n4", "b4", "n5", "b5", "n6", "b6", "n7", "b7", "n8", "b8"};

    SwitchedShuntData() {
        super(PowerFlowRecordGroup.SWITCHED_SHUNT);
        withFieldNames(V32, FIELD_NAMES_32_33);
        withFieldNames(V33, FIELD_NAMES_32_33);
        withFieldNames(V35, "i", "id", "modsw", "adjm", "stat", "vswhi", "vswlo", "swreg", "nreg", "rmpct", STR_RMIDNT, "binit", "s1", "n1", "b1", "s2", "n2", "b2", "s3", "n3", "b3", "s4", "n4", "b4", "s5", "n5", "b5", "s6", "n6", "b6", "s7", "n7", "b7", "s8", "n8", "b8");
        withQuotedFields("id", "shntid", STR_RMIDNT);
    }

    @Override
    protected Class<PsseSwitchedShunt> psseTypeClass() {
        return PsseSwitchedShunt.class;
    }
}
