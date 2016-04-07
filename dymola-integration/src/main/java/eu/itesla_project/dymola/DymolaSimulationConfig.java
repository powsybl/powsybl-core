/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DymolaSimulationConfig {

    /*

    #example of dymola-simulation.properties

    numberOfIntervals=0
    outputInterval=0.0
    method=Lsodar
    #method=Dassl
    tolerance=0.0001
    fixedStepSize=0.0

	*/


    private static final Logger LOGGER = LoggerFactory.getLogger(DymolaSimulationConfig.class);

    private static DymolaSimulationConfig INSTANCE;


    private int numberOfIntervals;
    private double outputInterval;
    private String method;
    private double tolerance;
    private double outputFixedstepSize;

    public synchronized static DymolaSimulationConfig load() {
        if (INSTANCE == null) {
            ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("dymola-simulation");

            int numberOfIntervals = config.getIntProperty("numberOfIntervals", 0);
            double outputInterval = config.getDoubleProperty("outputInterval", 0.0);
            String method = config.getStringProperty("method", "Lsodar");
            double tolerance = config.getDoubleProperty("tolerance", 0.0001);
            double outputFixedstepSize = config.getDoubleProperty("fixedStepSize", 0.0);


            INSTANCE = new DymolaSimulationConfig(numberOfIntervals, outputInterval, method, tolerance, outputFixedstepSize);
        }
        return INSTANCE;
    }

    public DymolaSimulationConfig(int numberOfIntervals,
                                  double outputInterval, String method, double tolerance, double outputFixedstepSize) {
        this.numberOfIntervals =numberOfIntervals;
        this.outputInterval =outputInterval;
        this.method =method;
        this.tolerance =tolerance;
        this.outputFixedstepSize =outputFixedstepSize;
    }

    public String getMethod() {
        return method;
    }

    public int getNumberOfIntervals() {
        return numberOfIntervals;
    }

    public double getOutputFixedstepSize() {
        return outputFixedstepSize;
    }

    public double getOutputInterval() {
        return outputInterval;
    }

    public double getTolerance() {
        return tolerance;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "[numberOfIntervals=" + numberOfIntervals +
                ", outputInterval=" + outputInterval +
                ", method=" + method +
                ", tolerance=" + tolerance +
                ", outputFixedstepSize=" + outputFixedstepSize +
                "]";
    }

}
