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

    static Line ofNullableLine(Line line) {
        if (line == null) {
            return null;
        }
        if (line.isTieLine()) {
            return ImmutableTieLine.ofNullable((TieLine) line);
        }
        return ImmutableLine.ofNullalbe(line);
    }

    static HvdcConverterStation ofNullableHvdcConverterStation(HvdcConverterStation cs) {
        if (cs instanceof LccConverterStation) {
            return ImmutableLccConverterStation.ofNullable((LccConverterStation) cs);
        } else if (cs instanceof VscConverterStation) {
            return ImmutableVscConverterStation.ofNullable((VscConverterStation) cs);
        } else {
            throw new PowsyblException("Invalid type " + cs.getClass() + " to be immutablized");
        }
    }

    static Connectable ofNullableConnectable(Connectable connectable) {
        if (connectable == null) {
            return null;
        }

        switch (connectable.getType()) {
            case BUSBAR_SECTION:
                return connectable;
            case LINE:
            case TWO_WINDINGS_TRANSFORMER:
                return ImmutableFactory.ofNullableBranch((Branch) connectable);
            case THREE_WINDINGS_TRANSFORMER:
                return ImmutableThreeWindingsTransformer.ofNullable((ThreeWindingsTransformer) connectable);
            case GENERATOR:
                return ImmutableGenerator.ofNullable((Generator) connectable);
            case LOAD:
                return ImmutableLoad.ofNullable((Load) connectable);
            case SHUNT_COMPENSATOR:
                return ImmutableShuntCompensator.ofNullable((ShuntCompensator) connectable);
            case DANGLING_LINE:
                return ImmutableDanglingLine.ofNullable((DanglingLine) connectable);
            case STATIC_VAR_COMPENSATOR:
                return ImmutableStaticVarCompensator.ofNullable((StaticVarCompensator) connectable);
            case HVDC_CONVERTER_STATION:
                return ImmutableFactory.ofNullableHvdcConverterStation((HvdcConverterStation) connectable);
            default:
                throw new IllegalArgumentException(connectable.getType().name() + " is not valid to be immutablized to line");
        }
    }

    static Branch ofNullableBranch(Branch b) {
        if (b == null) {
            return null;
        }
        switch (b.getType()) {
            case LINE:
                return ImmutableFactory.ofNullableLine((Line) b);
            case TWO_WINDINGS_TRANSFORMER:
                return ImmutableTwoWindingsTransformer.ofNullable((TwoWindingsTransformer) b);
            default:
                throw new IllegalArgumentException(b.getType().name() + " is not valid to be immutablized to branch");
        }
    }

    private ImmutableFactory() {
    }
}
