/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion.translation;

import com.powsybl.iidm.network.*;

import java.util.Optional;

public class DefaultNetworkElement implements NetworkElement<LoadingLimits> {

    private final Identifiable<?> identifiable;

    public DefaultNetworkElement(Identifiable<?> identifiable) {
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
                Country country1 = getCountry1();
                return country1 != null ? country1 : getCountry2();
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
    public Double getNominalVoltage1() {
        return getNominalVoltage(ThreeSides.ONE);
    }

    @Override
    public Double getNominalVoltage2() {
        return getNominalVoltage(ThreeSides.TWO);
    }

    @Override
    public Double getNominalVoltage3() {
        return getNominalVoltage(ThreeSides.THREE);
    }

    @Override
    public Double getNominalVoltage() {
        return getNominalVoltage1();
    }

    private Double getNominalVoltage(ThreeSides side) {
        return switch (identifiable.getType()) {
            case LINE -> ((Line) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel().getNominalV();
            case TIE_LINE -> ((TieLine) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel().getNominalV();
            case HVDC_LINE -> ((HvdcLine) identifiable).getConverterStation(side.toTwoSides()).getTerminal().getVoltageLevel().getNominalV();
            case TWO_WINDINGS_TRANSFORMER -> ((TwoWindingsTransformer) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel().getNominalV();
            case THREE_WINDINGS_TRANSFORMER -> ((ThreeWindingsTransformer) identifiable).getTerminal(side).getVoltageLevel().getNominalV();
            default -> null;
        };
    }

    @Override
    public Optional<LoadingLimits> getLimits(LimitType limitType, ThreeSides side) {
        return switch (identifiable.getType()) {
            case LINE -> (Optional<LoadingLimits>) ((Line) identifiable).getLimits(limitType, side.toTwoSides());
            case TIE_LINE -> (Optional<LoadingLimits>) ((TieLine) identifiable).getLimits(limitType, side.toTwoSides());
            case TWO_WINDINGS_TRANSFORMER -> (Optional<LoadingLimits>) ((TwoWindingsTransformer) identifiable).getLimits(limitType, side.toTwoSides());
            case THREE_WINDINGS_TRANSFORMER -> (Optional<LoadingLimits>) ((ThreeWindingsTransformer) identifiable).getLeg(side).getLimits(limitType);
            default -> Optional.empty();
        };
    }
}
