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

    AreaInterchangeData() {
        super(PowerFlowRecordGroup.AREA_INTERCHANGE);
        withFieldNames(V32, PsseArea.getFieldNames3233());
        withFieldNames(V33, PsseArea.getFieldNames3233());
        withFieldNames(V35, PsseArea.getFieldNames35());
        withQuotedFields(PsseArea.getFieldNamesString());
    }

    @Override
    protected Class<PsseArea> psseTypeClass() {
        return PsseArea.class;
    }
}
