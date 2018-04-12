/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.DanglingLine;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Xnode extends AbstractExtension<DanglingLine> {

    private final DanglingLine dl;

    private String code;

    public Xnode(DanglingLine dl, String code) {
        this.dl = Objects.requireNonNull(dl);
        this.code = Objects.requireNonNull(code);
    }

    @Override
    public String getName() {
        return "xnode";
    }

    @Override
    public DanglingLine getExtendable() {
        return dl;
    }

    public String getCode() {
        return code;
    }

    public Xnode setCode(String code) {
        this.code = Objects.requireNonNull(code);
        return this;
    }
}
