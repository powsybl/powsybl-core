/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.FACTS_CONTROL_DEVICE;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseFacts;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class FactsDeviceData extends AbstractRecordGroup<PsseFacts> {

    FactsDeviceData() {
        super(FACTS_CONTROL_DEVICE);
        withFieldNames(V33, "name", "i", "j", "mode", "pdes", "qdes", "vset", "shmx", "trmx", "vtmn", "vtmx", "vxmx", "imx", "linx",
            "rmpct", "owner", "set1", "set2", "vsref", "remot", "mname");
        withFieldNames(V35, "name", "ibus", "jbus", "mode", "pdes", "qdes", "vset", "shmx", "trmx", "vtmn", "vtmx", "vxmx", "imx", "linx",
            "rmpct", "owner", "set1", "set2", "vsref", "fcreg", "nreg", "mname");
        withQuotedFields("name", "mname");
    }

    @Override
    public Class<PsseFacts> psseTypeClass() {
        return PsseFacts.class;
    }
}
