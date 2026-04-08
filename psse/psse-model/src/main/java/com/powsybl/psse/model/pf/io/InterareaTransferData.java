/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.INTERAREA_TRANSFER;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseInterareaTransfer;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class InterareaTransferData extends AbstractRecordGroup<PsseInterareaTransfer> {

    InterareaTransferData() {
        super(INTERAREA_TRANSFER, "arfrom", "arto", "trid", "ptran");
        withQuotedFields("trid");
    }

    @Override
    protected Class<PsseInterareaTransfer> psseTypeClass() {
        return PsseInterareaTransfer.class;
    }
}
