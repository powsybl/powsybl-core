/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

/**
 * Parameters used to convert a contingency static description to a dynamic simulation scenario.
 *
 *               preFaultSimulationStopInstant   faultEventInstant     postFaultSimulationStopInstant
 *   |-----------|-------------------------------[***]------------------------|
 *   0s          50s                             70s 70.1s                    150s
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimulationParameters {

    private static final double DEFAULT_BRANCH_SHORT_CIRCUIT_DISTANCE = 50;
    private static final double DEFAULT_BRANCH_FAULT_RESISTANCE = 0;
    private static final double DEFAULT_BRANCH_FAULT_REACTANCE = 0.01;
    private static final double DEFAULT_GENERATOR_FAULT_RESISTANCE = 0.00001;
    private static final double DEFAULT_GENERATOR_FAULT_REACTANCE = 0.00001;

    private final double preFaultSimulationStopInstant;

    private final double faultEventInstant;

    private final double branchFaultShortCircuitDuration;

    private final double generatorFaultShortCircuitDuration;

    private final double postFaultSimulationStopInstant;

    private final double branchShortCircuitDistance;

    private final double branchFaultResistance;

    private final double branchFaultReactance;

    private final double generatorFaultResistance;

    private final double generatorFaultReactance;

    private final SimulationDetailedParameters detailedParameters;

    public static SimulationParameters load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("simulation-parameters");
        double preFaultSimulationStopInstant = config.getDoubleProperty("preFaultSimulationStopInstant");
        double faultEventInstant = config.getDoubleProperty("faultEventInstant");
        double branchFaultShortCircuitDuration = config.getDoubleProperty("branchFaultShortCircuitDuration");
        double generatorFaultShortCircuitDuration = config.getDoubleProperty("generatorFaultShortCircuitDuration");
        double postFaultSimulationStopInstant = config.getDoubleProperty("postFaultSimulationStopInstant");
        double branchShortCircuitDistance = config.getDoubleProperty("branchShortCircuitDistance", DEFAULT_BRANCH_SHORT_CIRCUIT_DISTANCE);
        double branchFaultResistance = config.getDoubleProperty("branchFaultResistance", DEFAULT_BRANCH_FAULT_RESISTANCE);
        double branchFaultReactance = config.getDoubleProperty("branchFaultReactance", DEFAULT_BRANCH_FAULT_REACTANCE);
        double generatorFaultResistance = config.getDoubleProperty("generatorFaultResistance", DEFAULT_GENERATOR_FAULT_RESISTANCE);
        double generatorFaultReactance = config.getDoubleProperty("generatorFaultReactance", DEFAULT_GENERATOR_FAULT_REACTANCE);
        String detailedParametersFileName = config.getStringProperty("detailsFileName", null);
        SimulationDetailedParameters detailedParameters = null;
        if (detailedParametersFileName != null) {
            detailedParameters = SimulationDetailedParameters.load(detailedParametersFileName);
        }
        return new SimulationParameters(preFaultSimulationStopInstant, faultEventInstant, branchFaultShortCircuitDuration,
                                        generatorFaultShortCircuitDuration, postFaultSimulationStopInstant, branchShortCircuitDistance,
                                        branchFaultResistance, branchFaultReactance, generatorFaultResistance, generatorFaultReactance,
                                        detailedParameters);
    }

    public SimulationParameters(double preFaultSimulationStopInstant, double faultEventInstant,
                                double branchFaultShortCircuitDuration, double generatorFaultShortCircuitDuration,
                                double postFaultSimulationStopInstant) {
        this(preFaultSimulationStopInstant, faultEventInstant, branchFaultShortCircuitDuration, generatorFaultShortCircuitDuration, postFaultSimulationStopInstant,
                DEFAULT_BRANCH_SHORT_CIRCUIT_DISTANCE, DEFAULT_BRANCH_FAULT_RESISTANCE, DEFAULT_BRANCH_FAULT_REACTANCE, DEFAULT_GENERATOR_FAULT_RESISTANCE, DEFAULT_GENERATOR_FAULT_REACTANCE, null);
    }

    public SimulationParameters(double preFaultSimulationStopInstant, double faultEventInstant,
                                double branchFaultShortCircuitDuration, double generatorFaultShortCircuitDuration,
                                double postFaultSimulationStopInstant, double branchShortCircuitDistance,
                                double branchFaultResistance, double branchFaultReactance,
                                double generatorFaultResistance, double generatorFaultReactance,
                                SimulationDetailedParameters detailedParameters) {
        if (preFaultSimulationStopInstant <= 0) {
            throw new IllegalArgumentException("preFaultSimulationStopInstant > 0 is expected");
        }
        if (faultEventInstant <= preFaultSimulationStopInstant) {
            throw new IllegalArgumentException("faultEventInstant > preFaultSimulationStopInstant is expected");
        }
        if (postFaultSimulationStopInstant <= faultEventInstant) {
            throw new IllegalArgumentException("postFaultSimulationStopInstant > faultEventInstant is expected");
        }
        if (branchFaultShortCircuitDuration <= 0) {
            throw new IllegalArgumentException("branchFaultShortCircuitDuration > 0 is expected");
        }
        if (generatorFaultShortCircuitDuration <= 0) {
            throw new IllegalArgumentException("generatorFaultShortCircuitDuration > 0 is expected");
        }
        if (branchShortCircuitDistance < 0 || branchShortCircuitDistance > 100) {
            throw new IllegalArgumentException("bad short curcuit distance " + branchShortCircuitDistance);
        }
        this.preFaultSimulationStopInstant = preFaultSimulationStopInstant;
        this.faultEventInstant = faultEventInstant;
        this.branchFaultShortCircuitDuration = branchFaultShortCircuitDuration;
        this.generatorFaultShortCircuitDuration = generatorFaultShortCircuitDuration;
        this.postFaultSimulationStopInstant = postFaultSimulationStopInstant;
        this.branchShortCircuitDistance = branchShortCircuitDistance;
        this.branchFaultResistance = branchFaultResistance;
        this.branchFaultReactance = branchFaultReactance;
        this.generatorFaultResistance = generatorFaultResistance;
        this.generatorFaultReactance = generatorFaultReactance;
        this.detailedParameters = detailedParameters;
    }

    public double getPreFaultSimulationStopInstant() {
        return preFaultSimulationStopInstant;
    }

    public double getFaultEventInstant() {
        return faultEventInstant;
    }

    public double getBranchFaultShortCircuitDuration() {
        return branchFaultShortCircuitDuration;
    }

    public double getGeneratorFaultShortCircuitDuration() {
        return generatorFaultShortCircuitDuration;
    }

    public double getPostFaultSimulationStopInstant() {
        return postFaultSimulationStopInstant;
    }

    public double getBranchShortCircuitDistance() {
        return branchShortCircuitDistance;
    }

    public double getBranchFaultResistance() {
        return branchFaultResistance;
    }

    public double getBranchFaultReactance() {
        return branchFaultReactance;
    }

    public double getGeneratorFaultResistance() {
        return generatorFaultResistance;
    }

    public double getGeneratorFaultReactance() {
        return generatorFaultReactance;
    }

    public SimulationDetailedParameters getDetailedParameters() {
        return detailedParameters;
    }

    public double getBranchFaultShortCircuitDistance(String contingencyId, String branchId) {
        Double distance = null;
        if (detailedParameters != null) {
            SimulationDetailedParameters.Contingency contingency = detailedParameters.getContingency(contingencyId);
            if (contingency != null) {
                SimulationDetailedParameters.Branch branch = contingency.getBranch(branchId);
                if (branch != null) {
                    distance = branch.getShortCircuitDistance();
                }
            }
        }
        return distance != null ? distance : branchShortCircuitDistance;
    }

    public double getBranchFaultShortCircuitDuration(String contingencyId, String branchId) {
        Double duration = null;
        if (detailedParameters != null) {
            SimulationDetailedParameters.Contingency contingency = detailedParameters.getContingency(contingencyId);
            if (contingency != null) {
                SimulationDetailedParameters.Branch branch = contingency.getBranch(branchId);
                if (branch != null) {
                    duration = branch.getShortCircuitDuration();
                }
            }
        }
        return duration != null ? duration : branchFaultShortCircuitDuration;
    }

    public String getBranchFaultShortCircuitSide(String contingencyId, String branchId) {
        String side = null;
        if (detailedParameters != null) {
            SimulationDetailedParameters.Contingency contingency = detailedParameters.getContingency(contingencyId);
            if (contingency != null) {
                SimulationDetailedParameters.Branch branch = contingency.getBranch(branchId);
                if (branch != null) {
                    side = branch.getShortCircuitSide();
                }
            }
        }
        return side;
    }

    public double getGeneratorFaultShortCircuitDuration(String contingencyId, String generatorId) {
        Double duration = null;
        if (detailedParameters != null) {
            SimulationDetailedParameters.Contingency contingency = detailedParameters.getContingency(contingencyId);
            if (contingency != null) {
                SimulationDetailedParameters.Generator generator = contingency.getGenerator(generatorId);
                if (generator != null) {
                    duration = generator.getShortCircuitDuration();
                }
            }
        }
        return duration != null ? duration : generatorFaultShortCircuitDuration;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [preFaultSimulationStopInstant=" + preFaultSimulationStopInstant +
                ", faultEventInstant=" + faultEventInstant +
                ", branchFaultShortCircuitDuration=" + branchFaultShortCircuitDuration +
                ", generatorFaultShortCircuitDuration=" + generatorFaultShortCircuitDuration +
                ", postFaultSimulationStopInstant=" + postFaultSimulationStopInstant +
                ", branchShortCircuitDistance=" + branchShortCircuitDistance +
                ", branchFaultResistance=" + branchFaultResistance +
                ", branchFaultReactance=" + branchFaultReactance +
                ", generatorFaultResistance=" + generatorFaultResistance +
                ", generatorFaultReactance=" + generatorFaultReactance +
                ", detailedParameters=" + Boolean.toString(detailedParameters != null) +
                "]";
    }
}
