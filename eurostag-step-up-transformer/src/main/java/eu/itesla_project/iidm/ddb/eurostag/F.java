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
class F {

    public final String name;
    public final float vbase;

    F(String name, float vbase) {
        this.name = name;
        this.vbase = vbase;
    }

    @Override
    public String toString() {
        return "F: name=" + name + ", vbase=" + vbase;
    }

    static F parse(String line) {
        Objects.requireNonNull(line, "line must not be null");
        if (!line.startsWith("F")) {
            throw new IllegalArgumentException("line is not a valid F record");
        }
        String name = line.substring(3, 11);
        float vbase = RecordUtil.parseFloat(line, 84, 92);
        return new F(name, vbase);
    }

}
