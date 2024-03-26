/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseArea;

import static com.powsybl.psse.model.PsseVersion.Major.V32;
import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class AreaInterchangeData extends AbstractRecordGroup<PsseArea> {

    private static final String[] FIELD_NAMES_32_33 = {"i", "isw", "pdes", "ptol", "arname"};

    AreaInterchangeData() {
        super(PowerFlowRecordGroup.AREA_INTERCHANGE);
        withFieldNames(V32, FIELD_NAMES_32_33);
        withFieldNames(V33, FIELD_NAMES_32_33);
        withFieldNames(V35, "iarea", "isw", "pdes", "ptol", "arname");
        withQuotedFields("arname");
    }

    @Override
    protected Class<PsseArea> psseTypeClass() {
        return PsseArea.class;
    }
}
