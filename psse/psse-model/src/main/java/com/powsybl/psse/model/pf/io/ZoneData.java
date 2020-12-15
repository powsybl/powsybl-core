/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseZone;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class ZoneData extends AbstractRecordGroup<PsseZone> {

    ZoneData() {
        super(PowerFlowRecordGroup.ZONE);
        withFieldNames(V33, "i", "zoname");
        withFieldNames(V35, "izone", "zoname");
        withQuotedFields("zoname");
    }

    @Override
    public Class<PsseZone> psseTypeClass() {
        return PsseZone.class;
    }
}
