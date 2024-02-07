/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.util.translation;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.translation.NetworkElement;

import java.util.Optional;

public class NetworkElementImpl implements NetworkElement {

    private final Identifiable<?> identifiable;

    NetworkElementImpl(Identifiable<?> identifiable) {
        this.identifiable = identifiable;
    }

    @Override
    public String getId() {
        return identifiable.getId();
    }

    @Override
    public Country getCountry1() {
        return getCountry(TwoSides.ONE);
    }

    @Override
    public Country getCountry2() {
        return getCountry(TwoSides.TWO);
    }

    @Override
    public Country getCountry() {
        switch (identifiable.getType()) {
            case TWO_WINDINGS_TRANSFORMER -> {
                Optional<Substation> substation = ((TwoWindingsTransformer) identifiable).getSubstation();
                return substation.map(Substation::getNullableCountry).orElse(null);
            }
            case THREE_WINDINGS_TRANSFORMER -> {
                Optional<Substation> substation = ((ThreeWindingsTransformer) identifiable).getSubstation();
                return substation.map(Substation::getNullableCountry).orElse(null);
            }
            default -> {
                return getCountry1() != null ? getCountry1() : getCountry2();
            }

        }
    }

    private Country getCountry(TwoSides side) {
        return switch (identifiable.getType()) {
            case LINE -> getCountryFromTerminal(((Line) identifiable).getTerminal(side));
            case TIE_LINE -> getCountryFromTerminal(((TieLine) identifiable).getDanglingLine(side).getTerminal());
            case HVDC_LINE -> getCountryFromTerminal(((HvdcLine) identifiable).getConverterStation(side).getTerminal());
            default -> null;
        };
    }

    private Country getCountryFromTerminal(Terminal terminal) {
        Optional<Substation> substation = terminal.getVoltageLevel().getSubstation();
        return substation.map(Substation::getNullableCountry).orElse(null);
    }

    @Override
    public VoltageLevel getVoltageLevel1() {
        return getVoltageLevel(ThreeSides.ONE);
    }

    @Override
    public VoltageLevel getVoltageLevel2() {
        return getVoltageLevel(ThreeSides.TWO);
    }

    @Override
    public VoltageLevel getVoltageLevel3() {
        return getVoltageLevel(ThreeSides.THREE);
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return getVoltageLevel1();
    }

    private VoltageLevel getVoltageLevel(ThreeSides side) {
        return switch (identifiable.getType()) {
            case LINE -> ((Line) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel();
            case TIE_LINE -> ((TieLine) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel();
            case HVDC_LINE -> ((HvdcLine) identifiable).getConverterStation(side.toTwoSides()).getTerminal().getVoltageLevel();
            case TWO_WINDINGS_TRANSFORMER -> ((TwoWindingsTransformer) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel();
            case THREE_WINDINGS_TRANSFORMER -> ((ThreeWindingsTransformer) identifiable).getTerminal(side).getVoltageLevel();
            default -> null;
        };
    }

    @Override
    public Optional<? extends LoadingLimits> getLoadingLimits(LimitType limitType, ThreeSides side) {
        return switch (identifiable.getType()) {
            case LINE -> ((Line) identifiable).getLimits(limitType, side.toTwoSides());
            case TIE_LINE -> ((TieLine) identifiable).getLimits(limitType, side.toTwoSides());
            //case HVDC_LINE -> ((HvdcLine) identifiable).getConverterStation(side.toTwoSides()).;
            case TWO_WINDINGS_TRANSFORMER -> ((TwoWindingsTransformer) identifiable).getLimits(limitType, side.toTwoSides());
            case THREE_WINDINGS_TRANSFORMER -> ((ThreeWindingsTransformer) identifiable).getLeg(side).getLimits(limitType);
            default -> Optional.empty();
        };
    }
}
