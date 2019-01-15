/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

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

    static HvdcConverterStation ofNullableHvdcConverterStation(HvdcConverterStation cs) {
        if (cs instanceof LccConverterStation) {
            return new ImmutableLccConverterStation((LccConverterStation) cs);
        } else if (cs instanceof VscConverterStation) {
            return new ImmutableVscConverterStation((VscConverterStation) cs);
        } else {
            throw new PowsyblException("Invalid type " + cs.getClass() + " to be immutablized");
        }
    }

    private ImmutableFactory() {
    }
}
