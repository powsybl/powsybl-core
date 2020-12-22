/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseSwitchedShunt;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class SwitchedShuntData extends AbstractRecordGroup<PsseSwitchedShunt> {

    SwitchedShuntData() {
        super(PowerFlowRecordGroup.SWITCHED_SHUNT);
        withFieldNames(V33, "i", "modsw", "adjm", "stat", "vswhi", "vswlo", "swrem", "rmpct", "rmidnt", "binit", "n1", "b1", "n2", "b2", "n3", "b3", "n4", "b4", "n5", "b5", "n6", "b6", "n7", "b7", "n8", "b8");
        withFieldNames(V35, "i", "id", "modsw", "adjm", "stat", "vswhi", "vswlo", "swreg", "nreg", "rmpct", "rmidnt", "binit", "s1", "n1", "b1", "s2", "n2", "b2", "s3", "n3", "b3", "s4", "n4", "b4", "s5", "n5", "b5", "s6", "n6", "b6", "s7", "n7", "b7", "s8", "n8", "b8");
        withQuotedFields("id", "rmidnt");
    }

    @Override
    public Class<PsseSwitchedShunt> psseTypeClass() {
        return PsseSwitchedShunt.class;
    }
}
