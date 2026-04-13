/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.elementmerging;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.vlequivalent.AbstractInjectionVlEquivalent;

import java.util.Objects;
import java.util.function.Function;

import static com.powsybl.iidm.modification.util.ModificationLogs.logOrThrow;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public abstract class AbstractInjectionTwoWindingTransformerMergeModification<I extends Injection<?>, A extends InjectionAdder<?, ?>, E extends AbstractInjectionVlEquivalent> extends AbstractNetworkModification {
    private final String voltageLevelId;

    protected AbstractInjectionTwoWindingTransformerMergeModification(String voltageLevelId) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            logOrThrow(throwException, String.format("Voltage level '%s' not found", voltageLevelId));
            return;
        }
        int numberOfConnectable = voltageLevel.getConnectableCount();
        int numberOfInjection = getInjectionCount(voltageLevel);
        int numberOfTransformer = voltageLevel.getTwoWindingsTransformerCount();
        if (numberOfConnectable != 2 || numberOfInjection != 1 || numberOfTransformer != 1) {
            logOrThrow(throwException, String.format(
                "Expecting number of (connectable, %s, transformer) to be (2, 1, 1) but got (%d, %d, %d) instead",
                getInjectionName(),
                numberOfConnectable,
                numberOfInjection,
                numberOfTransformer
            ));
            return;
        }

        //find the connection point in the other voltage level
        TwoWindingsTransformer transformer = voltageLevel.getTwoWindingsTransformers().iterator().next();
        I injection = getInjection(voltageLevel);
        Terminal oppositeTerminal = transformer.getTerminal1().getVoltageLevel().getId().equals(
            injection.getTerminal().getVoltageLevel().getId())
            ? transformer.getTerminal2() : transformer.getTerminal1();
        VoltageLevel oppositeVoltageLevel = oppositeTerminal.getVoltageLevel();

        A adder = createAndSetInjectionAdderParameters(voltageLevel, injection, transformer);
        if (oppositeVoltageLevel.getTopologyKind() == TopologyKind.BUS_BREAKER) {
            Bus bus = oppositeTerminal.getBusBreakerView().getBus();
            adder.setBus(bus.getId())
                .setConnectableBus(bus.getId());
        } else if (oppositeVoltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            adder.setNode(oppositeTerminal.getNodeBreakerView().getNode());
        } else {
            throw new IllegalStateException();
        }

        //TODO use modification.topology.RemoveVoltageLevel instead ?
        injection.remove();
        transformer.remove();
        voltageLevel.remove();

        //TODO check that if a breaker on the path was open, then the new load is not connected
        adder.add();
    }

    /**
     * Get the number of injections of the type of injection that is to be merged (not the number of all the injections)
     * @param voltageLevel the voltage level on which to get the injection of a type
     * @return the number of injection of a given type (depending on the element we want to merge with a two winding transformer)
     */
    abstract int getInjectionCount(VoltageLevel voltageLevel);

    abstract String getInjectionName();

    abstract Function<Double, A> getActivePowerSetter(A adder);

    private A setActivePower(A adder, double activePower) {
        return getActivePowerSetter(adder).apply(activePower);
    }

    abstract Function<Double, A> getReactivePowerSetter(A adder);

    private A setReactivePower(A adder, double reactivePower) {
        return getReactivePowerSetter(adder).apply(reactivePower);
    }

    private void setCommonInjectionParameters(A adder, E equivalent) {
        setActivePower(
            setReactivePower(adder, equivalent.getReactivePower()),
            equivalent.getActivePower()
        )
            .setFictitious(equivalent.isFictitious())
            .setId(equivalent.getId()) //TODO naming strategy ?
            .setName(equivalent.getName());
    }

    private A createAndSetInjectionAdderParameters(VoltageLevel voltageLevel, I injection, TwoWindingsTransformer transformer) {
        E equivalent = createEquivalent(injection, transformer);
        A adder = createAdder(voltageLevel);
        setCommonInjectionParameters(adder, equivalent);
        return setSpecificInjectionParameters(adder, equivalent);
    }

    abstract A setSpecificInjectionParameters(A adder, E equivalent);

    abstract E createEquivalent(I injection, TwoWindingsTransformer transformer);

    abstract A createAdder(VoltageLevel voltageLevel);

    abstract I getInjection(VoltageLevel voltageLevel);
}
