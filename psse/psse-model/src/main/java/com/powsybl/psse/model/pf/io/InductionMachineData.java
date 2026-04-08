/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseInductionMachine;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class InductionMachineData extends AbstractRecordGroup<PsseInductionMachine> {

    InductionMachineData() {
        super(PowerFlowRecordGroup.INDUCTION_MACHINE);
        withFieldNames(V33, "i", "id", "stat", "scode", "dcode", "area", "zone", "owner", "tcode", "bcode", "mbase", "ratekv", "pcode",
            "pset", "h", "a", "b", "d", "e", "ra", "xa", "xm", "r1", "x1", "r2", "x2", "x3", "e1", "se1", "e2", "se2", "ia1", "ia2", "xamult");
        withFieldNames(V35, "ibus", "imid", "stat", "scode", "dcode", "area", "zone", "owner", "tcode", "bcode", "mbase", "ratekv", "pcode",
            "pset", "hconst", "aconst", "bconst", "dconst", "econst", "ra", "xa", "xm", "r1", "x1", "r2", "x2", "x3", "e1", "se1",
            "e2", "se2", "ia1", "ia2", "xamult");
        withQuotedFields("id", "imid");
    }

    @Override
    protected Class<PsseInductionMachine> psseTypeClass() {
        return PsseInductionMachine.class;
    }
}
