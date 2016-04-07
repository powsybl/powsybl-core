/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LH {

    public final String znodlo;
    public final float pl;
    public final float ql;

    LH(String znodlo, float pl, float ql) {
        this.znodlo = znodlo;
        this.pl = pl;
        this.ql = ql;
    }

    @Override
    public String toString() {
        return "LH: znodlo=" + znodlo + ", pl=" + pl + ", ql=" + ql;
    }

    static LH parse(String line) {
        Objects.requireNonNull(line, "line must not be null");
        if (!line.startsWith("LH")) {
            throw new IllegalArgumentException("line is not a valid LH record");
        }
        String znodlo = line.substring(14, 22);
        float pl = Float.parseFloat(line.substring(23, 31));
        float ql = Float.parseFloat(line.substring(32, 40));
        return new LH(znodlo, pl, ql);
    }

}
