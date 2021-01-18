/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseOwner;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class OwnerData extends AbstractRecordGroup<PsseOwner> {

    OwnerData() {
        super(PowerFlowRecordGroup.OWNER);
        withFieldNames(V33, "i", "owname");
        withFieldNames(V35, "iowner", "owname");
        withQuotedFields("owname");
    }

    @Override
    public Class<PsseOwner> psseTypeClass() {
        return PsseOwner.class;
    }
}
