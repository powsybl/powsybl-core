/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LoadingLimitsAdder;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.stream.Collectors;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;

import java.util.stream.Stream;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class LoadingLimitsMapping {

    // JAM TODO Version update
    protected final Map<String, LoadingLimitsAdder<?, ?>> adders = new HashMap<>();
    private final Map<String, List<Rc>> operationalLimitsMapping = new HashMap<>();

    // JAM TODO Nuevo Main
    private final Context context;
    private final Map<OperationalLimitsGroup, CurrentLimitsAdder> currentLimitsAdders;
    private final Map<OperationalLimitsGroup, ActivePowerLimitsAdder> activePowerLimitsAdders;
    private final Map<OperationalLimitsGroup, ApparentPowerLimitsAdder> apparentPowerLimitsAdders;

    LoadingLimitsMapping(Context context) {
        this.context = Objects.requireNonNull(context);
        this.currentLimitsAdders = new HashMap<>();
        this.activePowerLimitsAdders = new HashMap<>();
        this.apparentPowerLimitsAdders = new HashMap<>();
    }

    /**
     * Get or create the limit adder for the given limits group and limit subclass.
     * Different CGMES OperationalLimit can be mapped to the same IIDM LoadingLimit,
     * hence the need to retrieve the limit adder if it has already been created before.
     * @param limitsGroup The OperationalLimitsGroup to which the LoadingLimitsAdder shall add the limits.
     * @param limitSubClass The operational limit subclass indicating which LoadingLimitsAdder subclass shall be used.
     * @return The LoadingLimitsAdder for the given limits group and limit subclass.
     */
    public LoadingLimitsAdder getLoadingLimitsAdder(OperationalLimitsGroup limitsGroup, String limitSubClass) {
        return switch (limitSubClass) {
            case CgmesNames.CURRENT_LIMIT -> currentLimitsAdders.computeIfAbsent(limitsGroup, s -> limitsGroup.newCurrentLimits());
            case CgmesNames.ACTIVE_POWER_LIMIT -> activePowerLimitsAdders.computeIfAbsent(limitsGroup, s -> limitsGroup.newActivePowerLimits());
            case CgmesNames.APPARENT_POWER_LIMIT -> apparentPowerLimitsAdders.computeIfAbsent(limitsGroup, s -> limitsGroup.newApparentPowerLimits());
            default -> throw new IllegalArgumentException();
        };
    }

    public void addOperationalLimit(String operationalLimitId, String identifiableId, String end, String limitSubclass, String limitType, int duration, double normalValue) {
        operationalLimitsMapping.computeIfAbsent(identifiableId, s -> new ArrayList<>()).add(new Rc(operationalLimitId, end, limitSubclass, limitType, duration, normalValue));
    }

    public void addOperationalLimitProperties(Network network) {
        Set<Rcd> operationalLimitsProperties = operationalLimitsMapping.values().stream().flatMap(List::stream).toList().stream()
                .filter(Rc::isValid).map(Rc::convertToRcd).collect(Collectors.toSet());
        operationalLimitsProperties.stream().sorted(Comparator.comparing(rcd -> rcd.operationalLimitId)).forEach(rcd -> addOperationalLimitProperty(network, rcd));

        operationalLimitsMapping.forEach((key, valueList) -> addOperationalLimitPropertiesForIdentifiable(network, key, valueList));
    }

    private static void addOperationalLimitProperty(Network network, Rcd rcd) {
        if (rcd.duration > 0) {
            network.setProperty(getOperationalLimitDurationKey(rcd.operationalLimitId), String.valueOf(rcd.duration));
        }
        if (!Double.isNaN(rcd.normalValue)) {
            network.setProperty(getOperationalLimitNormalValueKey(rcd.operationalLimitId), String.valueOf(rcd.normalValue));
        }
    }

    public static String getOperationalLimitDurationKey(String operationalLimitId) {
        return Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + operationalLimitId + "-" + "duration";
    }

    public static String getOperationalLimitNormalValueKey(String operationalLimitId) {
        return Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + operationalLimitId + "-" + "normalValue";
    }

    private static void addOperationalLimitPropertiesForIdentifiable(Network network, String identifiableId, List<Rc> rcList) {
        Identifiable<?> identifiable = network.getIdentifiable(identifiableId);
        if (identifiable != null) {
            addPropertiesForVoltageOperationalLimits(identifiable, rcList);

            addPropertiesForLoadingOperationalLimits(identifiable, rcList, "ActivePowerLimit");
            addPropertiesForLoadingOperationalLimits(identifiable, rcList, "ApparentPowerLimit");
            addPropertiesForLoadingOperationalLimits(identifiable, rcList, "CurrentLimit");
        }
    }

    private static void addPropertiesForVoltageOperationalLimits(Identifiable<?> identifiable, List<Rc> rcList) {
        addPropertiesForSetOfOperationalLimits(identifiable, rcList, "", "VoltageLimit", "highVoltage");
        addPropertiesForSetOfOperationalLimits(identifiable, rcList, "", "VoltageLimit", "lowVoltage");
    }

    private static void addPropertiesForLoadingOperationalLimits(Identifiable<?> identifiable, List<Rc> rcList, String limitSubclass) {
        addPropertiesForSetOfOperationalLimits(identifiable, rcList, "", limitSubclass, "patl");
        addPropertiesForSetOfOperationalLimits(identifiable, rcList, "", limitSubclass, "tatl");

        addPropertiesForSetOfOperationalLimits(identifiable, rcList, "1", limitSubclass, "patl");
        addPropertiesForSetOfOperationalLimits(identifiable, rcList, "1", limitSubclass, "tatl");
        addPropertiesForSetOfOperationalLimits(identifiable, rcList, "2", limitSubclass, "patl");
        addPropertiesForSetOfOperationalLimits(identifiable, rcList, "2", limitSubclass, "tatl");
        addPropertiesForSetOfOperationalLimits(identifiable, rcList, "3", limitSubclass, "patl");
        addPropertiesForSetOfOperationalLimits(identifiable, rcList, "3", limitSubclass, "tatl");
    }

    private static void addPropertiesForSetOfOperationalLimits(Identifiable<?> identifiable, List<Rc> rcList, String end, String limitSubclass, String limitType) {
        List<Rc> selectedRcList = rcList.stream().filter(rc -> rc.isValid(end, limitSubclass, limitType)).toList();
        if (!selectedRcList.isEmpty()) {
            identifiable.setProperty(getOperationalLimitKey(end, limitSubclass, limitType), getOperationalLimitsIdsString(selectedRcList));
        }
    }

    public static String getOperationalLimitKey(String end, String limitSubclass, String limitType) {
        return "operationalLimit" + "-" + end + "-" + limitSubclass + "-" + limitType;
    }

    private static String getOperationalLimitsIdsString(List<Rc> selectedRcList) {
        return String.join(";", selectedRcList.stream().map(rc -> rc.operationalLimitId).toList());
    }

    public static List<String> splitOperationalLimitsIdsString(String operationalLimitsIdsString) {
        return Arrays.stream(operationalLimitsIdsString.split(";")).toList();
    }

    /**
     * Execute the add method for all the valid LoadingLimitsAdder stored in this mapping class.
     * This method shall be called after all the CGMES OperationalLimit have been converted.
     */
    void addAll() {
        Stream.of(currentLimitsAdders, activePowerLimitsAdders, apparentPowerLimitsAdders)
                .flatMap(m -> m.values().stream())
                .filter(adder -> !Double.isNaN(adder.getPermanentLimit()) || adder.hasTemporaryLimits())
                .forEach(adder -> adder.fixLimits(context.config().getMissingPermanentLimitPercentage(), context::fixed)
                        .add());

        Stream.of(currentLimitsAdders, activePowerLimitsAdders, apparentPowerLimitsAdders).forEach(Map::clear);
    }

    private record Rc(String operationalLimitId, String end, String limitSubclass, String limitType, int duration,
                      double normalValue) {
        private boolean isValid() {
            return duration != 0.0 || !Double.isNaN(normalValue);
        }

        private boolean isValid(String end, String limitSubclass, String limitType) {
            return this.end.equals(end) && this.limitSubclass.equals(limitSubclass) && this.limitType.equals(limitType);
        }

        private Rcd convertToRcd() {
            return new Rcd(operationalLimitId, duration, normalValue);
        }
    }

    private record Rcd(String operationalLimitId, int duration, double normalValue) {
    }
}
