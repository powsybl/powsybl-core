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

    private static final String[] FIELD_NAMES_32_33 = {"i", "j", "ckt", "r", "x", "b", "ratea", "rateb", "ratec", "gi", "bi", "gj", "bj", "st", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};

    NonTransformerBranchData() {
        super(PowerFlowRecordGroup.NON_TRANSFORMER_BRANCH);
        withFieldNames(V32, FIELD_NAMES_32_33);
        withFieldNames(V33, FIELD_NAMES_32_33);
        withFieldNames(V35, "ibus", "jbus", "ckt", "rpu", "xpu", "bpu", "name", "rate1", "rate2", "rate3", "rate4", "rate5", "rate6", "rate7", "rate8", "rate9", "rate10", "rate11", "rate12", "gi", "bi", "gj", "bj", "stat", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4");
        withQuotedFields("ckt", "name");
    }

    @Override
    protected Class<PsseNonTransformerBranch> psseTypeClass() {
        return PsseNonTransformerBranch.class;
    }
}
