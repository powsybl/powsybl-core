/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseLoad;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V32;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class LoadData extends AbstractRecordGroup<PsseLoad> {
    private static final String STR_ID = "id";
    private static final String STR_AREA = "area";
    private static final String STR_ZONE = "zone";
    private static final String STR_PL = "pl";
    private static final String STR_QL = "ql";
    private static final String STR_IP = "ip";
    private static final String STR_IQ = "iq";
    private static final String STR_YP = "yp";
    private static final String STR_OWNER = "owner";
    private static final String STR_SCALE = "scale";

    LoadData() {
        super(PowerFlowRecordGroup.LOAD);
        withFieldNames(V32, "i", STR_ID, "status", STR_AREA, STR_ZONE, STR_PL, STR_QL, STR_IP, STR_IQ, STR_YP, "yq", STR_OWNER, STR_SCALE);
        withFieldNames(V33, "i", STR_ID, "status", STR_AREA, STR_ZONE, STR_PL, STR_QL, STR_IP, STR_IQ, STR_YP, "yq", STR_OWNER, STR_SCALE, "intrpt");
        withFieldNames(V35, "ibus", "loadid", "stat", STR_AREA, STR_ZONE, STR_PL, STR_QL, STR_IP, STR_IQ, STR_YP, "yq", STR_OWNER, STR_SCALE, "intrpt", "dgenp", "dgenq", "dgenm", "loadtype");
        withQuotedFields(STR_ID, "loadid", "loadtype");
    }

    @Override
    protected Class<PsseLoad> psseTypeClass() {
        return PsseLoad.class;
    }
}
