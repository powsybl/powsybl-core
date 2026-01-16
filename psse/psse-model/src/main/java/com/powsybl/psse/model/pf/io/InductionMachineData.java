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
        withFieldNames(V33, PsseInductionMachine.getFieldNames33());
        withFieldNames(V35, PsseInductionMachine.getFieldNames35());
        withQuotedFields(PsseInductionMachine.getFieldNamesString());
    }

    @Override
    protected Class<PsseInductionMachine> psseTypeClass() {
        return PsseInductionMachine.class;
    }
}
