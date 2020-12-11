/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.data;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseGenerator;
import com.powsybl.psse.model.PsseVersion;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class GeneratorData extends AbstractRecordGroup<PsseGenerator> {

    private static final String[] FIELD_NAMES_35 = {"ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg", "nreg", "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "baslod", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
    private static final String[] FIELD_NAMES_33 = {"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg", "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
    private static final String[] QUOTED_FIELDS_35 = {"machid"};
    private static final String[] QUOTED_FIELDS_33 = {"id"};

    GeneratorData() {
        super(PsseRecordGroup.GENERATOR);
    }

    @Override
    public String[] fieldNames(PsseVersion version) {
        switch (version.major()) {
            case V35:
                return FIELD_NAMES_35;
            case V33:
                return FIELD_NAMES_33;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    @Override
    public String[] quotedFields(PsseVersion version) {
        switch (version.major()) {
            case V35:
                return QUOTED_FIELDS_35;
            case V33:
                return QUOTED_FIELDS_33;
            default:
                throw new PsseException("Unsupported version " + version);
        }
    }

    @Override
    public Class<PsseGenerator> psseTypeClass() {
        return PsseGenerator.class;
    }
}
