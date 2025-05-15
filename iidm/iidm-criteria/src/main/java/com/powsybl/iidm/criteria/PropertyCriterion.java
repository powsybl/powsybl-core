/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class PropertyCriterion implements Criterion {
    private final String propertyKey;
    private final List<String> propertyValues;
    private final EquipmentToCheck equipmentToCheck;
    private final SideToCheck sideToCheck;

    public enum EquipmentToCheck {
        SELF,
        VOLTAGE_LEVEL,
        SUBSTATION
    }

    public enum SideToCheck {
        ONE,
        BOTH,
        ALL_THREE
    }

    public PropertyCriterion(String propertyKey, List<String> propertyValues,
                             EquipmentToCheck equipmentToCheck) {
        this(propertyKey, propertyValues, equipmentToCheck, null);
    }

    public PropertyCriterion(String propertyKey, List<String> propertyValues,
                             EquipmentToCheck equipmentToCheck, SideToCheck sideToCheck) {
        this.propertyKey = Objects.requireNonNull(propertyKey);
        this.propertyValues = ImmutableList.copyOf(propertyValues);
        this.equipmentToCheck = Objects.requireNonNull(equipmentToCheck);
        this.sideToCheck = sideToCheck;
    }

    @Override
    public CriterionType getType() {
        return CriterionType.PROPERTY;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        return switch (equipmentToCheck) {
            case SELF ->
                    identifiable.hasProperty(propertyKey) && propertyValues.contains(identifiable.getProperty(propertyKey));
            case VOLTAGE_LEVEL, SUBSTATION -> filterEquipment(identifiable, type);
        };
    }

    private boolean filterIdentifiable(Identifiable<?> identifiable) {
        return identifiable.hasProperty(propertyKey) && propertyValues.contains(identifiable.getProperty(propertyKey));
    }

    private boolean filterEquipment(Identifiable<?> identifiable, IdentifiableType type) {
        // TODO DcConverter
        return switch (type) {
            case STATIC_VAR_COMPENSATOR, SHUNT_COMPENSATOR, BUSBAR_SECTION, GENERATOR, DANGLING_LINE, LOAD, BATTERY, HVDC_CONVERTER_STATION ->
                    filterSubstationOrVoltageLevel(((Injection<?>) identifiable).getTerminal().getVoltageLevel());
            case SWITCH -> filterSubstationOrVoltageLevel(((Switch) identifiable).getVoltageLevel());
            case TWO_WINDINGS_TRANSFORMER, LINE ->
                    filterBranch(((Branch<?>) identifiable).getTerminal1().getVoltageLevel(),
                            ((Branch<?>) identifiable).getTerminal2().getVoltageLevel());
            case HVDC_LINE ->
                    filterBranch(((HvdcLine) identifiable).getConverterStation1().getTerminal().getVoltageLevel(),
                            ((HvdcLine) identifiable).getConverterStation2().getTerminal().getVoltageLevel());
            case THREE_WINDINGS_TRANSFORMER -> filterThreeWindingsTransformer((ThreeWindingsTransformer) identifiable);
            default ->
                    throw new PowsyblException(String.format("type %s has no implementation for ContingencyElement", type));
        };
    }

    private boolean filterBranch(VoltageLevel voltageLevel1, VoltageLevel voltageLevel2) {
        return switch (sideToCheck) {
            case ONE -> filterSubstationOrVoltageLevel(voltageLevel1) || filterSubstationOrVoltageLevel(voltageLevel2);
            case BOTH -> filterSubstationOrVoltageLevel(voltageLevel1) && filterSubstationOrVoltageLevel(voltageLevel2);
            default ->
                    throw new IllegalArgumentException("only ONE or BOTH sides can be checked when filtering properties on branches");
        };
    }

    private boolean filterThreeWindingsTransformer(ThreeWindingsTransformer threeWindingsTransformer) {
        VoltageLevel voltageLevel1 = threeWindingsTransformer.getLeg1().getTerminal().getVoltageLevel();
        VoltageLevel voltageLevel2 = threeWindingsTransformer.getLeg2().getTerminal().getVoltageLevel();
        VoltageLevel voltageLevel3 = threeWindingsTransformer.getLeg3().getTerminal().getVoltageLevel();
        if (sideToCheck == null) {
            throw new IllegalArgumentException("enum to check side can not be null for threeWindingsTransformer to check their voltage level");
        }
        return switch (sideToCheck) {
            case ONE ->
                    filterSubstationOrVoltageLevel(voltageLevel1) || filterSubstationOrVoltageLevel(voltageLevel2) ||
                            filterSubstationOrVoltageLevel(voltageLevel3);
            case BOTH -> filterSubstationOrVoltageLevel(voltageLevel1) && filterSubstationOrVoltageLevel(voltageLevel2)
                    || filterSubstationOrVoltageLevel(voltageLevel1) && filterSubstationOrVoltageLevel(voltageLevel3)
                    || filterSubstationOrVoltageLevel(voltageLevel2) && filterSubstationOrVoltageLevel(voltageLevel3);
            case ALL_THREE ->
                    filterSubstationOrVoltageLevel(voltageLevel1) && filterSubstationOrVoltageLevel(voltageLevel2) &&
                            filterSubstationOrVoltageLevel(voltageLevel3);
            default ->
                    throw new IllegalArgumentException("enum to check side must be ONE, BOTH or ALL_THREE for threeWindingsTransformer");
        };
    }

    private boolean filterSubstationOrVoltageLevel(VoltageLevel voltageLevel) {
        if (equipmentToCheck == EquipmentToCheck.VOLTAGE_LEVEL) {
            return filterIdentifiable(voltageLevel);
        } else {
            Substation substation = voltageLevel.getNullableSubstation();
            return substation != null && filterIdentifiable(substation);
        }
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public List<String> getPropertyValues() {
        return propertyValues;
    }

    public EquipmentToCheck getEquipmentToCheck() {
        return equipmentToCheck;
    }

    public SideToCheck getSideToCheck() {
        return sideToCheck;
    }
}
