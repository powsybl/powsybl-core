/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
final class TopologyModificationUtils {

    static final class LoadingLimitsBags {

        private LoadingLimitsBag activePowerLimits;
        private LoadingLimitsBag apparentPowerLimits;
        private LoadingLimitsBag currentLimits;

        LoadingLimitsBags() {
        }

        LoadingLimitsBags(Supplier<Optional<ActivePowerLimits>> activePowerLimitsGetter, Supplier<Optional<ApparentPowerLimits>> apparentPowerLimitsGetter,
                          Supplier<Optional<CurrentLimits>> currentLimitsGetter) {
            activePowerLimits = activePowerLimitsGetter.get().map(LoadingLimitsBag::new).orElse(null);
            apparentPowerLimits = apparentPowerLimitsGetter.get().map(LoadingLimitsBag::new).orElse(null);
            currentLimits = currentLimitsGetter.get().map(LoadingLimitsBag::new).orElse(null);
        }

        Optional<LoadingLimitsBag> getActivePowerLimits() {
            return Optional.ofNullable(activePowerLimits);
        }

        Optional<LoadingLimitsBag> getApparentPowerLimits() {
            return Optional.ofNullable(apparentPowerLimits);
        }

        Optional<LoadingLimitsBag> getCurrentLimits() {
            return Optional.ofNullable(currentLimits);
        }

        void setActivePowerLimits(LoadingLimitsBag activePowerLimits) {
            this.activePowerLimits = activePowerLimits;
        }

        void setApparentPowerLimits(LoadingLimitsBag apparentPowerLimits) {
            this.apparentPowerLimits = apparentPowerLimits;
        }

        void setCurrentLimits(LoadingLimitsBag currentLimits) {
            this.currentLimits = currentLimits;
        }
    }

    static final class LoadingLimitsBag {

        private double permanentLimit;
        private List<TemporaryLimitsBag> temporaryLimits = new ArrayList<>();

        LoadingLimitsBag() {
        }

        private LoadingLimitsBag(LoadingLimits limits) {
            this.permanentLimit = limits.getPermanentLimit();
            for (LoadingLimits.TemporaryLimit tl : limits.getTemporaryLimits()) {
                temporaryLimits.add(new TemporaryLimitsBag(tl));
            }
        }

        double getPermanentLimit() {
            return permanentLimit;
        }

        void setPermanentLimit(double permanentLimit) {
            this.permanentLimit = permanentLimit;
        }

        private List<TemporaryLimitsBag> getTemporaryLimits() {
            return ImmutableList.copyOf(temporaryLimits);
        }

        void setTemporaryLimits(List<TemporaryLimitsBag> temporaryLimits) {
            this.temporaryLimits = temporaryLimits;
        }

    }

    static final class TemporaryLimitsBag {

        private final String name;
        private final int acceptableDuration;
        private final boolean fictitious;
        private final double value;

        TemporaryLimitsBag(LoadingLimits.TemporaryLimit temporaryLimit) {
            this.name = temporaryLimit.getName();
            this.acceptableDuration = temporaryLimit.getAcceptableDuration();
            this.fictitious = temporaryLimit.isFictitious();
            this.value = temporaryLimit.getValue();
        }

        String getName() {
            return name;
        }

        int getAcceptableDuration() {
            return acceptableDuration;
        }

        boolean isFictitious() {
            return fictitious;
        }

        double getValue() {
            return value;
        }
    }

    static double checkPercent(double percent) {
        if (Double.isNaN(percent)) {
            throw new PowsyblException("Percent should not be undefined");
        }
        return percent;
    }

    static LineAdder createLineAdder(double percent, String id, String name, String voltageLevelId1, String voltageLevelId2, Network network, Line line) {
        return network.newLine()
                .setId(id)
                .setName(name)
                .setVoltageLevel1(voltageLevelId1)
                .setVoltageLevel2(voltageLevelId2)
                .setR(line.getR() * percent / 100)
                .setX(line.getX() * percent / 100)
                .setG1(line.getG1() * percent / 100)
                .setB1(line.getB1() * percent / 100)
                .setG2(line.getG2() * percent / 100)
                .setB2(line.getB2() * percent / 100);
    }

    static LineAdder createLineAdder(String id, String name, String voltageLevelId1, String voltageLevelId2, Network network, Line line1, Line line2) {
        return network.newLine()
                .setId(id)
                .setName(name)
                .setVoltageLevel1(voltageLevelId1)
                .setVoltageLevel2(voltageLevelId2)
                .setR(line1.getR() + line2.getR())
                .setX(line1.getX() + line2.getX())
                .setG1(line1.getG1() + line2.getG1())
                .setB1(line1.getB1() + line2.getB1())
                .setG2(line1.getG2() + line2.getG2())
                .setB2(line1.getB2() + line2.getB2());
    }

    static void attachLine(Terminal terminal, LineAdder adder, BiConsumer<Bus, LineAdder> connectableBusSetter,
                           BiConsumer<Bus, LineAdder> busSetter, BiConsumer<Integer, LineAdder> nodeSetter) {
        if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.BUS_BREAKER) {
            connectableBusSetter.accept(terminal.getBusBreakerView().getConnectableBus(), adder);
            Bus bus = terminal.getBusBreakerView().getBus();
            if (bus != null) {
                busSetter.accept(bus, adder);
            }
        } else if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            int node = terminal.getNodeBreakerView().getNode();
            nodeSetter.accept(node, adder);
        } else {
            throw new AssertionError();
        }
    }

    static void addLoadingLimits(Line created, LoadingLimitsBags limits, Branch.Side side) {
        if (side == Branch.Side.ONE) {
            limits.getActivePowerLimits().ifPresent(lim -> addLoadingLimits(created.newActivePowerLimits1(), lim));
            limits.getApparentPowerLimits().ifPresent(lim -> addLoadingLimits(created.newApparentPowerLimits1(), lim));
            limits.getCurrentLimits().ifPresent(lim -> addLoadingLimits(created.newCurrentLimits1(), lim));
        } else {
            limits.getActivePowerLimits().ifPresent(lim -> addLoadingLimits(created.newActivePowerLimits2(), lim));
            limits.getApparentPowerLimits().ifPresent(lim -> addLoadingLimits(created.newApparentPowerLimits2(), lim));
            limits.getCurrentLimits().ifPresent(lim -> addLoadingLimits(created.newCurrentLimits2(), lim));
        }
    }

    private static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> void addLoadingLimits(A adder, LoadingLimitsBag limits) {
        adder.setPermanentLimit(limits.getPermanentLimit());
        for (TemporaryLimitsBag tl : limits.getTemporaryLimits()) {
            adder.beginTemporaryLimit()
                    .setName(tl.getName())
                    .setAcceptableDuration(tl.getAcceptableDuration())
                    .setFictitious(tl.isFictitious())
                    .setValue(tl.getValue())
                    .endTemporaryLimit();
        }
        adder.add();
    }

    static void report(String message, String key, TypedValue typedValue, Reporter reporter) {
        reporter.report(Report.builder()
                .withKey(key)
                .withDefaultMessage(message)
                .withSeverity(typedValue)
                .build());
    }

    static void removeVoltageLevelAndSubstation(VoltageLevel voltageLevel, Reporter reporter) {
        Optional<Substation> substation = voltageLevel.getSubstation();
        if (voltageLevel.getConnectableStream().noneMatch(c -> c.getType() != IdentifiableType.BUSBAR_SECTION && c.getType() != IdentifiableType.BUS)) {
            String vlId = voltageLevel.getId();
            voltageLevel.remove();
            report(String.format("Voltage level %s removed", vlId), "voltageLevelRemoved", TypedValue.INFO_SEVERITY, reporter);
        }
        substation.ifPresent(s -> {
            if (Iterables.isEmpty(s.getVoltageLevels())) {
                s.remove();
                report(String.format("Substation %s removed", s.getId()), "substationRemoved", TypedValue.INFO_SEVERITY, reporter);
            }
        });
    }

    static LoadingLimitsBag calcMinLoadingLimitsBag(List<LoadingLimits> loadingLimits) {
        LoadingLimitsBag limit = new LoadingLimitsBag();
        List<TemporaryLimitsBag> temporaryLimitsBags = new ArrayList<>();
        Map<Integer, TemporaryLimitsBag> temporaryLimitsByAcceptableDuration = new HashMap<>();
        double minPermanentLimitValue = Double.MAX_VALUE;

        for (LoadingLimits loadingLimit : loadingLimits) {
            // find the min permanent limit value
            if (loadingLimit.getPermanentLimit() < minPermanentLimitValue) {
                minPermanentLimitValue = loadingLimit.getPermanentLimit();
            }

            // find the min temporary limit value for each acceptableDuration
            Collection<LoadingLimits.TemporaryLimit> temporaryLimits = loadingLimit.getTemporaryLimits();
            for (LoadingLimits.TemporaryLimit temporaryLimit : temporaryLimits) {
                Integer acceptableDuration = temporaryLimit.getAcceptableDuration();
                if (!temporaryLimitsByAcceptableDuration.containsKey(acceptableDuration)) {
                    temporaryLimitsByAcceptableDuration.put(acceptableDuration, new TemporaryLimitsBag(temporaryLimit));
                }
                if (temporaryLimit.getValue() < temporaryLimitsByAcceptableDuration.get(acceptableDuration).getValue()) {
                    temporaryLimitsByAcceptableDuration.put(acceptableDuration, new TemporaryLimitsBag(temporaryLimit));
                }
            }
            temporaryLimitsBags.addAll(temporaryLimitsByAcceptableDuration.values());
        }

        limit.setPermanentLimit(minPermanentLimitValue);
        limit.setTemporaryLimits(temporaryLimitsBags);

        return limit;
    }

    static LoadingLimitsBags calcMinLoadingLimitsBags(Line line1, Line line2) {
        LoadingLimitsBags limits = new LoadingLimitsBags();

        List<LoadingLimits> currentLimits = Stream.of(line1.getCurrentLimits1(), line1.getCurrentLimits2(), line2.getCurrentLimits1(), line2.getCurrentLimits2())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (!currentLimits.isEmpty()) {
            limits.setCurrentLimits(calcMinLoadingLimitsBag(currentLimits));
        }

        List<LoadingLimits> activePowerLimits = Stream.of(line1.getActivePowerLimits1(), line1.getActivePowerLimits2(), line2.getActivePowerLimits1(), line2.getActivePowerLimits2())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (!activePowerLimits.isEmpty()) {
            limits.setActivePowerLimits(calcMinLoadingLimitsBag(activePowerLimits));
        }

        List<LoadingLimits> apparentPowerLimits = Stream.of(line1.getApparentPowerLimits1(), line1.getApparentPowerLimits2(), line2.getApparentPowerLimits1(), line2.getApparentPowerLimits2())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (!apparentPowerLimits.isEmpty()) {
            limits.setApparentPowerLimits(calcMinLoadingLimitsBag(apparentPowerLimits));
        }

        return limits;
    }

    static void createNodeBreakerSwitches(int node1, int middleNode, int node2, String lineId, VoltageLevel.NodeBreakerView view) {
        createNodeBreakerSwitches(node1, middleNode, node2, "", lineId, view);
    }

    static void createNodeBreakerSwitches(int node1, int middleNode, int node2, String suffix, String lineId, VoltageLevel.NodeBreakerView view) {
        view.newSwitch()
                .setId(lineId + "_BREAKER" + suffix)
                .setKind(SwitchKind.BREAKER)
                .setOpen(false)
                .setRetained(true)
                .setNode1(node1)
                .setNode2(middleNode)
                .add();
        view.newSwitch()
                .setId(lineId + "_DISCONNECTOR" + suffix)
                .setKind(SwitchKind.DISCONNECTOR)
                .setOpen(false)
                .setNode1(middleNode)
                .setNode2(node2)
                .add();
    }

    static void createBusBreakerSwitches(String busId1, String middleBusId, String busId2, String lineId, VoltageLevel.BusBreakerView view) {
        view.newSwitch()
                .setId(lineId + "_SW_1")
                .setOpen(false)
                .setBus1(busId1)
                .setBus2(middleBusId)
                .add();
        view.newSwitch()
                .setId(lineId + "_SW_2")
                .setOpen(false)
                .setBus1(middleBusId)
                .setBus2(busId2)
                .add();
    }

    private TopologyModificationUtils() {
    }
}
