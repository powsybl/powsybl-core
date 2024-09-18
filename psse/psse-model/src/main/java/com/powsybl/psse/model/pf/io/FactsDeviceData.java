/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import static com.powsybl.psse.model.PsseVersion.Major.V32;
import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.FACTS_CONTROL_DEVICE;
import static com.powsybl.psse.model.pf.io.PsseIoConstants.STR_MNAME;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseFacts;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class FactsDeviceData extends AbstractRecordGroup<PsseFacts> {

    private static final String[] FIELD_NAMES_32_33 = {"name", "i", "j", "mode", "pdes", "qdes", "vset", "shmx", "trmx", "vtmn", "vtmx", "vxmx", "imx", "linx", "rmpct", "owner", "set1", "set2", "vsref", "remot", STR_MNAME};
    static final String[] FIELD_NAMES_35 = {"name", "ibus", "jbus", "mode", "pdes", "qdes", "vset", "shmx", "trmx", "vtmn", "vtmx", "vxmx", "imx", "linx", "rmpct", "owner", "set1", "set2", "vsref", "fcreg", "nreg", STR_MNAME};

    FactsDeviceData() {
        super(FACTS_CONTROL_DEVICE);
        withFieldNames(V32, FIELD_NAMES_32_33);
        withFieldNames(V33, FIELD_NAMES_32_33);
        withFieldNames(V35, FIELD_NAMES_35);
        withQuotedFields("name", STR_MNAME);
    }

    @Override
    protected Class<PsseFacts> psseTypeClass() {
        return PsseFacts.class;
    }
}
