/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.DanglingLine;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Xnode extends Extension<DanglingLine> {

    String NAME = "xnode";

    @Override
    default String getName() {
        return NAME;
    }

    String getCode();

    // No need for CRTP style return type returning a more specific adder
    // because this interface is not meant to be extended.
    Xnode setCode(String code);

}
