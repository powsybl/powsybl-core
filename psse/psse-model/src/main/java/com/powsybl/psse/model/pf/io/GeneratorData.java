/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.pf.io;

import com.powsybl.psse.model.io.AbstractRecordGroup;
import com.powsybl.psse.model.pf.PsseGenerator;

import static com.powsybl.psse.model.PsseVersion.Major.V33;
import static com.powsybl.psse.model.PsseVersion.Major.V35;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class GeneratorData extends AbstractRecordGroup<PsseGenerator> {

    GeneratorData() {
        super(PowerFlowRecordGroup.GENERATOR);
        withFieldNames(V33, "i", "id", "pg", "qg", "qt", "qb", "vs", "ireg", "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf");
        withFieldNames(V35, "ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg", "nreg", "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "baslod", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf");
        withQuotedFields("id", "machid");
    }

    @Override
    public Class<PsseGenerator> psseTypeClass() {
        return PsseGenerator.class;
    }
}
