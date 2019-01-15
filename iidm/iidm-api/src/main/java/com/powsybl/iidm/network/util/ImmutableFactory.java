/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TieLine;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableFactory {

    public static Line ofNullableLine(Line line) {
        if (line == null) {
            return null;
        }
        if (line.isTieLine()) {
            return new ImmutableTieLine((TieLine) line);
        }
        return ImmutableLine.ofNullalbe(line);
    }

    private ImmutableFactory() {
    }
}
