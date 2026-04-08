/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseZone {

    @Parsed(field = {"i", "izone"})
    private int i;

    @Parsed(defaultNullRead = "            ")
    private String zoname;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getZoname() {
        return zoname;
    }

    public void setZoname(String zoname) {
        this.zoname = zoname;
    }
}
