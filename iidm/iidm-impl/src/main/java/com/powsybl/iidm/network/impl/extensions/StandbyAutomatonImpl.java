package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class StandbyAutomatonImpl extends AbstractExtension<StaticVarCompensator> implements StandbyAutomaton {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandbyAutomatonImpl.class);

    /**
     * b0 is the susceptance of the static var compensator when it behaves like a shunt compensator.
     * This behaviour is active when following boolean standby is set to true.
     * b0 should be greater than Bmin and lower than Bmax.
     */
    private double b0;

    /**
     * true if the static var compensator behaves like a shunt compensator of susceptance b0 and monitors
     * voltage at regulating terminal.
     */
    private boolean standby;

    /**
     * Voltage target when the voltage at regulating terminal becomes lower that the low voltage threshold.
     */
    private double lowVoltageSetPoint;

    /**
     * Voltage target when the voltage at regulating terminal becomes greater that the high voltage threshold.
     */
    private double highVoltageSetPoint;

    /**
     * The low voltage threshold in kV.
     */
    private double lowVoltageThreshold;

    /**
     * The high voltage threshold in kV.
     */
    private double highVoltageThreshold;

    private static double checkB0(double v) {
        if (Double.isNaN(v)) {
            throw new IllegalArgumentException("b0 is invalid");
        }
        return v;
    }

    private static void checkVoltageConfig(double lowVoltageSetPoint, double highVoltageSetPoint,
                                           double lowVoltageThreshold, double highVoltageThreshold) {
        if (Double.isNaN(lowVoltageSetPoint)) {
            throw new IllegalArgumentException("lowVoltageSetPoint is invalid");
        }
        if (Double.isNaN(highVoltageSetPoint)) {
            throw new IllegalArgumentException("highVoltageSetPoint is invalid");
        }
        if (Double.isNaN(lowVoltageThreshold)) {
            throw new IllegalArgumentException("lowVoltageThreshold is invalid");
        }
        if (Double.isNaN(highVoltageThreshold)) {
            throw new IllegalArgumentException("highVoltageThreshold is invalid");
        }
        if (lowVoltageThreshold >= highVoltageThreshold) {
            throw new IllegalArgumentException("Inconsistent low (" + lowVoltageThreshold + ") and high (" + highVoltageThreshold + ") voltage thresholds");
        }
        if (lowVoltageSetPoint < lowVoltageThreshold) {
            LOGGER.warn("Invalid low voltage setpoint {} < threshold {}", lowVoltageSetPoint, lowVoltageThreshold);
        }
        if (highVoltageSetPoint > highVoltageThreshold) {
            LOGGER.warn("Invalid high voltage setpoint {} > threshold {}", highVoltageSetPoint, highVoltageThreshold);
        }
    }

    public StandbyAutomatonImpl(StaticVarCompensator svc, double b0, boolean standby, double lowVoltageSetPoint, double highVoltageSetPoint,
                                double lowVoltageThreshold, double highVoltageThreshold) {
        super(svc);
        this.b0 = checkB0(b0);
        this.standby = standby;
        checkVoltageConfig(lowVoltageSetPoint, highVoltageSetPoint, lowVoltageThreshold, highVoltageThreshold);
        this.lowVoltageSetPoint = lowVoltageSetPoint;
        this.highVoltageSetPoint = highVoltageSetPoint;
        this.lowVoltageThreshold = lowVoltageThreshold;
        this.highVoltageThreshold = highVoltageThreshold;
    }

    @Override
    public boolean isStandby() {
        return standby;
    }

    @Override
    public StandbyAutomatonImpl setStandby(boolean standby) {
        this.standby = standby;
        return this;
    }

    @Override
    public double getB0() {
        return b0;
    }

    @Override
    public StandbyAutomatonImpl setB0(double b0) {
        this.b0 = checkB0(b0);
        return this;
    }

    @Override
    public double getHighVoltageSetPoint() {
        return highVoltageSetPoint;
    }

    @Override
    public StandbyAutomatonImpl setHighVoltageSetPoint(double highVoltageSetPoint) {
        checkVoltageConfig(lowVoltageSetPoint, highVoltageSetPoint, lowVoltageThreshold, highVoltageThreshold);
        this.highVoltageSetPoint = highVoltageSetPoint;
        return this;
    }

    @Override
    public double getHighVoltageThreshold() {
        return highVoltageThreshold;
    }

    @Override
    public StandbyAutomatonImpl setHighVoltageThreshold(double highVoltageThreshold) {
        checkVoltageConfig(lowVoltageSetPoint, highVoltageSetPoint, lowVoltageThreshold, highVoltageThreshold);
        this.highVoltageThreshold = highVoltageThreshold;
        return this;
    }

    @Override
    public double getLowVoltageSetPoint() {
        return lowVoltageSetPoint;
    }

    @Override
    public StandbyAutomatonImpl setLowVoltageSetPoint(double lowVoltageSetPoint) {
        checkVoltageConfig(lowVoltageSetPoint, highVoltageSetPoint, lowVoltageThreshold, highVoltageThreshold);
        this.lowVoltageSetPoint = lowVoltageSetPoint;
        return this;
    }

    @Override
    public double getLowVoltageThreshold() {
        return lowVoltageThreshold;
    }

    @Override
    public StandbyAutomatonImpl setLowVoltageThreshold(double lowVoltageThreshold) {
        checkVoltageConfig(lowVoltageSetPoint, highVoltageSetPoint, lowVoltageThreshold, highVoltageThreshold);
        this.lowVoltageThreshold = lowVoltageThreshold;
        return this;
    }
}
