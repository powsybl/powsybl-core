/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseNonTransformerBranch;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V32;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class NonTransformerBranchData extends AbstractRecordGroup<PsseNonTransformerBranch> {

    NonTransformerBranchData() {
        super(PowerFlowRecordGroup.NON_TRANSFORMER_BRANCH);
        withFieldNames(V32, PsseNonTransformerBranch.getFieldNames3233());
        withFieldNames(V33, PsseNonTransformerBranch.getFieldNames3233());
        withFieldNames(V35, PsseNonTransformerBranch.getFieldNames35());
        withQuotedFields(PsseNonTransformerBranch.getFieldNamesString());
    }

    @Override
    protected Class<PsseNonTransformerBranch> psseTypeClass() {
        return PsseNonTransformerBranch.class;
    }
}
