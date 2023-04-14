/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.modification.generation.dispatch;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.ReportBuilder;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.util.ConnectedComponents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class GenerationDispatch extends AbstractNetworkModification {
    private static final String COMPONENT = "CC";
    private static final String POWER_TO_DISPATCH = "PowerToDispatch";
    private static final String STACKING = "Stacking";
    private static final String RESULT = "Result";

    private final double lossCoefficient;  // loss coefficient (between 0 and 100)

    private final Map<Integer, List<Generator>> fixedSupplyGenerators = new HashMap<>();
    private final Map<Integer, List<Generator>> adjustableGenerators = new HashMap<>();

    private final Map<Integer, Double> totalDemand = new HashMap<>();
    private final Map<Integer, Double> remainingPowerImbalance = new HashMap<>();

    GenerationDispatch(double lossCoefficient) {
        this.lossCoefficient = lossCoefficient;
    }

    private void report(Reporter reporter, String key, String defaultMessage, Map<String, Object> values, TypedValue severity) {
        ReportBuilder builder = Report.builder()
                .withKey(key)
                .withDefaultMessage(defaultMessage)
                .withSeverity(severity);
        for (Map.Entry<String, Object> valueEntry : values.entrySet()) {
            builder.withValue(valueEntry.getKey(), valueEntry.getValue().toString());
        }
        reporter.report(builder.build());
    }

    public double getRemainingPowerImbalance(int numCC) {
        return remainingPowerImbalance.get(numCC);
    }

    public double getTotalDemand(int numCC) {
        return totalDemand.get(numCC);
    }

    public double computeTotalDemand(Component component) {
        double totalLoad = ConnectedComponents.computeTotalActiveLoad(component);
        return totalLoad * (1. + lossCoefficient / 100.);
    }

    public double computeTotalAmountFixedSupply(int numCC) {
        double totalAmountFixedSupply = 0.;
        totalAmountFixedSupply += fixedSupplyGenerators.get(numCC).stream().filter(generator -> generator.getTerminal().isConnected())
                    .mapToDouble(Generator::getTargetP).sum();
        return totalAmountFixedSupply;
    }

    public void computeAdjustableGenerators(Component component, Reporter reporter) {
        List<Generator> generators = new ArrayList<>();

        // get all connected generators in the component
        for (Bus bus : component.getBuses()) {
            generators.addAll(bus.getGeneratorStream().filter(generator -> generator.getTerminal().isConnected())
                    .collect(Collectors.toList()));
        }
        // remove non adjustable generators (empty list in this first version)
        generators.removeAll(fixedSupplyGenerators.get(component.getNum()));

        // remove generators without marginal cost
        adjustableGenerators.put(component.getNum(), generators.stream().filter(generator -> {
            GeneratorStartup startupExtension = generator.getExtension(GeneratorStartup.class);
            boolean marginalCostAvailable = startupExtension != null && !Double.isNaN(startupExtension.getMarginalCost());
            if (!marginalCostAvailable) {
                report(reporter, "MissingMarginalCostForGenerator", "The generator ${generator} does not have a marginal cost",
                        Map.of("generator", generator.getId()), TypedValue.WARN_SEVERITY);
            }
            return marginalCostAvailable;
        }).collect(Collectors.toList()));

        // sort generators by marginal cost, and then by alphabetic order of id
        adjustableGenerators.get(component.getNum()).sort(Comparator.comparing(generator -> ((Generator) generator).getExtension(GeneratorStartup.class).getMarginalCost())
                .thenComparing(generator -> ((Generator) generator).getId()));

        if (adjustableGenerators.get(component.getNum()).isEmpty()) {
            report(reporter, "NoAvailableAdjustableGenerator", "There is no adjustable generator",
                        Map.of(), TypedValue.WARN_SEVERITY);
        }
    }

    private class GeneratorTargetPListener extends DefaultNetworkListener {
        private final Reporter reporter;

        GeneratorTargetPListener(Reporter reporter) {
            this.reporter = reporter;
        }

        @Override
        public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
            if (identifiable.getType() == IdentifiableType.GENERATOR &&
                    attribute.equals("targetP") &&
                    (double) oldValue != (double) newValue) {
                report(reporter, "GeneratorSetTargetP", "Generator ${generator} targetP : ${oldValue} MW --> ${newValue} MW",
                        Map.of("generator", identifiable.getId(), "oldValue", oldValue, "newValue", newValue), TypedValue.INFO_SEVERITY);
            }
        }
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        Collection<Component> connectedComponents = network.getBusView().getConnectedComponents();
        for (Component component : connectedComponents) {
            int componentNum = component.getNum();

            remainingPowerImbalance.put(componentNum, 0.);
            fixedSupplyGenerators.put(componentNum, new ArrayList<>());  // no fixed supply generators in this first version

            Reporter componentReporter = reporter.createSubReporter(COMPONENT + componentNum, COMPONENT + componentNum);

            Reporter powerToDispatchReporter = componentReporter.createSubReporter(POWER_TO_DISPATCH, POWER_TO_DISPATCH);

            // get total value of connected loads in the connected component
            totalDemand.put(componentNum, computeTotalDemand(component));
            report(powerToDispatchReporter, "TotalDemand", "The total demand is : ${totalDemand} MW",
                    Map.of("totalDemand", totalDemand.get(componentNum)), TypedValue.INFO_SEVERITY);

            // get total supply value for non adjustable generators (will be 0. in this first version)
            double totalAmountFixedSupply = computeTotalAmountFixedSupply(componentNum);
            report(powerToDispatchReporter, "TotalAmountFixedSupply", "The total amount of fixed supply is : ${totalAmountFixedSupply} MW",
                    Map.of("totalAmountFixedSupply", totalAmountFixedSupply), TypedValue.INFO_SEVERITY);

            double totalAmountSupplyToBeDispatched = totalDemand.get(componentNum) - totalAmountFixedSupply;
            if (totalAmountSupplyToBeDispatched < 0.) {
                report(powerToDispatchReporter, "TotalAmountFixedSupplyExceedsTotalDemand", "The total amount of fixed supply exceeds the total demand",
                    Map.of(), TypedValue.WARN_SEVERITY);
                continue;
            } else {
                report(powerToDispatchReporter, "TotalAmountSupplyToBeDispatched", "The total amount of supply to be dispatched is : ${totalAmountSupplyToBeDispatched} MW",
                    Map.of("totalAmountSupplyToBeDispatched", totalAmountSupplyToBeDispatched), TypedValue.INFO_SEVERITY);
            }

            // get adjustable generators in the component
            computeAdjustableGenerators(component, powerToDispatchReporter);
            if (adjustableGenerators.get(componentNum).isEmpty()) {
                continue;
            }

            // set targetP to 0 for all adjustable generators
            adjustableGenerators.get(componentNum).forEach(generator -> generator.setTargetP(0.));

            // stacking of adjustable generators to ensure the totalAmountSupplyToBeDispatched
            List<Scalable> generatorsScalable = adjustableGenerators.get(componentNum).stream().map(generator ->
                (Scalable) Scalable.onGenerator(generator.getId(), generator.getMinP(), generator.getMaxP())
            ).collect(Collectors.toList());

            Reporter stackingReporter = componentReporter.createSubReporter(STACKING, STACKING);

            GeneratorTargetPListener listener = new GeneratorTargetPListener(stackingReporter);
            network.addListener(listener);

            Scalable scalable = Scalable.stack(generatorsScalable.toArray(Scalable[]::new));
            double realized = scalable.scale(network, totalAmountSupplyToBeDispatched);

            network.removeListener(listener);

            Reporter resultReporter = componentReporter.createSubReporter(RESULT, RESULT);

            if (realized == totalAmountSupplyToBeDispatched) {
                report(resultReporter, "SupplyDemandBalanceCouldBeMet", "The supply-demand balance could be met",
                    Map.of(), TypedValue.INFO_SEVERITY);
            } else {
                remainingPowerImbalance.put(componentNum, totalAmountSupplyToBeDispatched - realized);
                report(resultReporter, "SupplyDemandBalanceCouldNotBeMet", "The supply-demand balance could not be met : the remaining power imbalance is ${remainingPower} MW",
                    Map.of("remainingPower", remainingPowerImbalance.get(componentNum)), TypedValue.WARN_SEVERITY);
            }
        }
    }
}
