/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseGenerator35 extends PsseGenerator {

    @Parsed
    private int nreg = 0;

    @Parsed
    private int baslod = 0;

    public int getNreg() {
        return nreg;
    }

    public void setNreg(int nreg) {
        this.nreg = nreg;
    }

    public int getBasLod() {
        return baslod;
    }

    public void setBaslod(int baslod) {
        this.baslod = baslod;
    }
}
