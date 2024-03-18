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

import java.util.List;
import java.util.Optional;

/**
 * <p>Adapter to have a {@link NetworkElement} from an {@link Identifiable} object.</p>
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class DefaultNetworkElementAdapter implements NetworkElement {
    private static final List<IdentifiableType> EVERY_SUPPORTED_TYPES = List.of(
            IdentifiableType.LINE,
            IdentifiableType.TIE_LINE,
            IdentifiableType.HVDC_LINE,
            IdentifiableType.TWO_WINDINGS_TRANSFORMER,
            IdentifiableType.THREE_WINDINGS_TRANSFORMER,
            IdentifiableType.DANGLING_LINE,
            IdentifiableType.GENERATOR,
            IdentifiableType.LOAD,
            IdentifiableType.BATTERY,
            IdentifiableType.SHUNT_COMPENSATOR,
            IdentifiableType.STATIC_VAR_COMPENSATOR,
            IdentifiableType.BUSBAR_SECTION,
            IdentifiableType.HVDC_CONVERTER_STATION
    );

    private final Identifiable<?> identifiable;

    public DefaultNetworkElementAdapter(Identifiable<?> identifiable) {
        this.identifiable = identifiable;
    }

    @Override
    public String getId() {
        return identifiable.getId();
    }

    @Override
    public Optional<Country> getCountry1() {
        return getCountry(TwoSides.ONE);
    }

    @Override
    public Optional<Country> getCountry2() {
        return getCountry(TwoSides.TWO);
    }

    @Override
    public Optional<Country> getCountry() {
        return getCountry1();
    }

    private Optional<Country> getCountry(TwoSides side) {
        return switch (identifiable.getType()) {
            case LINE, TIE_LINE -> getCountryFromTerminal(((Branch<?>) identifiable).getTerminal(side));
            case HVDC_LINE -> getCountryFromTerminal(((HvdcLine) identifiable).getConverterStation(side).getTerminal());
            case DANGLING_LINE, GENERATOR, LOAD, BATTERY, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, HVDC_CONVERTER_STATION ->
                    side != TwoSides.ONE ? Optional.empty() : getCountryFromTerminal(((Injection<?>) identifiable).getTerminal());
            case TWO_WINDINGS_TRANSFORMER -> side != TwoSides.ONE ? Optional.empty() :
                ((TwoWindingsTransformer) identifiable).getSubstation().map(Substation::getNullableCountry);
            case THREE_WINDINGS_TRANSFORMER -> side != TwoSides.ONE ? Optional.empty() :
                ((ThreeWindingsTransformer) identifiable).getSubstation().map(Substation::getNullableCountry);
            default -> Optional.empty();
        };
    }

    private Optional<Country> getCountryFromTerminal(Terminal terminal) {
        Optional<Substation> substation = terminal.getVoltageLevel().getSubstation();
        return substation.map(Substation::getNullableCountry);
    }

    @Override
    public Optional<Double> getNominalVoltage1() {
        return getNominalVoltage(ThreeSides.ONE);
    }

    @Override
    public Optional<Double> getNominalVoltage2() {
        return getNominalVoltage(ThreeSides.TWO);
    }

    @Override
    public Optional<Double> getNominalVoltage3() {
        return getNominalVoltage(ThreeSides.THREE);
    }

    @Override
    public Optional<Double> getNominalVoltage() {
        return getNominalVoltage1();
    }

    private Optional<Double> getNominalVoltage(ThreeSides side) {
        return switch (identifiable.getType()) {
            case DANGLING_LINE, GENERATOR, LOAD, BATTERY, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, HVDC_CONVERTER_STATION ->
                    side != ThreeSides.ONE ? Optional.empty() :
                            Optional.of(((Injection<?>) identifiable).getTerminal().getVoltageLevel().getNominalV());
            case LINE, TIE_LINE, TWO_WINDINGS_TRANSFORMER ->
                    side == ThreeSides.THREE ? Optional.empty() :
                            Optional.of(((Branch<?>) identifiable).getTerminal(side.toTwoSides()).getVoltageLevel().getNominalV());
            case HVDC_LINE ->
                    side == ThreeSides.THREE ? Optional.empty() :
                            Optional.of(((HvdcLine) identifiable).getConverterStation(side.toTwoSides()).getTerminal().getVoltageLevel().getNominalV());
            case THREE_WINDINGS_TRANSFORMER ->
                    Optional.of(((ThreeWindingsTransformer) identifiable).getTerminal(side).getVoltageLevel().getNominalV());
            default -> Optional.empty();
        };
    }

    @Override
    public boolean isValidFor(NetworkElementCriterionType type) {
        return type == NetworkElementCriterionType.IDENTIFIER
                || type == NetworkElementCriterionType.LINE
                    && identifiable.getType() == IdentifiableType.LINE
                || type == NetworkElementCriterionType.TIE_LINE
                    && identifiable.getType() == IdentifiableType.TIE_LINE
                || type == NetworkElementCriterionType.DANGLING_LINE
                    && identifiable.getType() == IdentifiableType.DANGLING_LINE
                || type == NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER
                    && identifiable.getType() == IdentifiableType.TWO_WINDINGS_TRANSFORMER
                || type == NetworkElementCriterionType.THREE_WINDINGS_TRANSFORMER
                    && identifiable.getType() == IdentifiableType.THREE_WINDINGS_TRANSFORMER
                || type == NetworkElementCriterionType.IDENTIFIABLE
                    && EVERY_SUPPORTED_TYPES.contains(identifiable.getType());
    }

    protected Identifiable<?> getIdentifiable() {
        return identifiable;
    }
}
