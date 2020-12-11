/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseBus;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;

import java.io.OutputStream;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class BusData extends AbstractRecordGroup<PsseBus> {

    private static final String[] FIELD_NAMES_35 = {"ibus", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va", "nvhi", "nvlo", "evhi", "evlo"};
    private static final String[] FIELD_NAMES_33 = {"i", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va", "nvhi", "nvlo", "evhi", "evlo"};
    private static final String[] QUOTED_FIELDS = {"name"};

    BusData() {
        super(PsseRecordGroup.BUS);
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

    @Override
    public String[] quotedFields(PsseVersion version) {
        switch (version) {
            case VERSION_35:
            case VERSION_33:
                return QUOTED_FIELDS;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    @Override
    public Class<PsseBus> psseTypeClass() {
        return PsseBus.class;
    }

    protected void writeBegin(OutputStream outputStream) {
        // We do not want to write a begin comment for Bus data records
    }
}
