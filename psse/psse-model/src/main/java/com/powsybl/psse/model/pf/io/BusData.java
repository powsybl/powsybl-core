/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.io.RecordGroupIOLegacyText;
import com.powsybl.psse.model.pf.PsseBus;

import java.io.OutputStream;

import static com.powsybl.psse.model.PsseVersion.Major.V32;
import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.io.FileFormat.LEGACY_TEXT;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class BusData extends AbstractRecordGroup<PsseBus> {

    static final String[] FIELD_NAMES_BUS_35 = {"ibus", STR_NAME, STR_BASKV, STR_IDE, STR_AREA, STR_ZONE, STR_OWNER, STR_VM, STR_VA, "nvhi", "nvlo", "evhi", "evlo"};

    BusData() {
        super(PowerFlowRecordGroup.BUS);
        withFieldNames(V32, "i", STR_NAME, STR_BASKV, STR_IDE, STR_AREA, STR_ZONE, STR_OWNER, STR_VM, STR_VA);
        withFieldNames(V33, "i", STR_NAME, STR_BASKV, STR_IDE, STR_AREA, STR_ZONE, STR_OWNER, STR_VM, STR_VA, "nvhi", "nvlo", "evhi", "evlo");
        withFieldNames(V35, FIELD_NAMES_BUS_35);
        withQuotedFields(STR_NAME);
        withIO(LEGACY_TEXT, new BusLegacyText(this));
    }

    @Override
    protected Class<PsseBus> psseTypeClass() {
        return PsseBus.class;
    }

    private static class BusLegacyText extends RecordGroupIOLegacyText<PsseBus> {
        protected BusLegacyText(AbstractRecordGroup<PsseBus> recordGroup) {
            super(recordGroup);
        }

        @Override
        protected void writeBegin(OutputStream outputStream) {
            // We do not want to write a begin comment for Bus data records
        }
    }
}
