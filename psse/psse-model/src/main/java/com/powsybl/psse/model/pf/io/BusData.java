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

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class BusData extends AbstractRecordGroup<PsseBus> {

    BusData() {
        super(PowerFlowRecordGroup.BUS);
        withFieldNames(V32, "i", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va");
        withFieldNames(V33, "i", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va", "nvhi", "nvlo", "evhi", "evlo");
        withFieldNames(V35, "ibus", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va", "nvhi", "nvlo", "evhi", "evlo");
        withQuotedFields("name");
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
