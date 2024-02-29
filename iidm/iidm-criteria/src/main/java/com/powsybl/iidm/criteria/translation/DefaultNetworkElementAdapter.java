/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.translation;

import com.powsybl.iidm.criteria.NetworkElementCriterion.NetworkElementCriterionType;
import com.powsybl.iidm.network.*;

import java.util.Optional;
import java.util.Set;

/**
 * <p>Adapter to have a {@link NetworkElement} from an {@link Identifiable} object.</p>
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class DefaultNetworkElementAdapter implements NetworkElement {

    private static final Set<IdentifiableType> VALID_TYPES_FOR_LINE_CRITERION = Set.of(IdentifiableType.LINE, IdentifiableType.TIE_LINE);

    private final Identifiable<?> identifiable;

    public DefaultNetworkElementAdapter(Identifiable<?> identifiable) {
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
            case TWO_WINDINGS_TRANSFORMER -> ((TwoWindingsTransformer) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel().getNominalV();
            case THREE_WINDINGS_TRANSFORMER -> ((ThreeWindingsTransformer) identifiable).getTerminal(side).getVoltageLevel().getNominalV();
            default -> null;
        };
    }

    @Override
    public boolean isValidFor(NetworkElementCriterionType type) {
        return type == NetworkElementCriterionType.IDENTIFIERS
                || type == NetworkElementCriterionType.LINE
                    && VALID_TYPES_FOR_LINE_CRITERION.contains(identifiable.getType())
                || type == NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER
                    && identifiable.getType() == IdentifiableType.TWO_WINDINGS_TRANSFORMER
                || type == NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER
                    && identifiable.getType() == IdentifiableType.THREE_WINDINGS_TRANSFORMER;
    }

    protected Identifiable<?> getIdentifiable() {
        return identifiable;
    }
}
