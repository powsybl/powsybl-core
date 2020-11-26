/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseNonTransformerBranch;
import com.powsybl.psse.model.PsseNonTransformerBranch35;
import com.powsybl.psse.model.PsseVersion;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class NonTransformerBranchData extends AbstractRecordGroup<PsseNonTransformerBranch> {

    private static final String[] FIELD_NAMES_35 = {"ibus", "jbus", "ckt", "rpu", "xpu", "bpu", "name", "rate1", "rate2", "rate3", "rate4", "rate5",
        "rate6", "rate7", "rate8", "rate9", "rate10", "rate11", "rate12", "gi", "bi", "gj", "bj",
        "stat", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
    private static final String[] FIELD_NAMES_33 = {"i", "j", "ckt", "r", "x", "b", "ratea", "rateb", "ratec", "gi", "bi", "gj", "bj",
        "st", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};

    NonTransformerBranchData() {
        super(PsseRecordGroup.NON_TRANSFORMER_BRANCH_DATA);
    }

    @Override
    public Class<? extends PsseNonTransformerBranch> psseTypeClass(PsseVersion version) {
        if (version == PsseVersion.VERSION_35) {
            return PsseNonTransformerBranch35.class;
        } else {
            return PsseNonTransformerBranch.class;
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
