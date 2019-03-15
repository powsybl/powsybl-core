package com.powsybl.cgmes.conversion.elements.full;

import java.util.ArrayList;
import java.util.List;

import com.powsybl.iidm.network.PhaseTapChanger;

public class TapChanger {

    private int                            lowTapPosition              = 0;
    private Integer                        tapPosition;
    private final List<StepAdder>          steps                       = new ArrayList<>();
    private boolean                        loadTapChangingCapabilities = false;
    private PhaseTapChanger.RegulationMode regulationMode              = PhaseTapChanger.RegulationMode.FIXED_TAP;
    private boolean                        regulating                  = false;
    private double                         regulationValue             = Double.NaN;
    private String                         regulationTerminal;

    class StepAdder {

        private double angleRad = 0.0;
        private double ratio    = 1.0;
        private double r        = 0.0;
        private double x        = 0.0;
        private double g1       = 0.0;
        private double b1       = 0.0;
        private double g2       = 0.0;
        private double b2       = 0.0;

        public StepAdder setAngle(double angle) {
            this.angleRad = angle;
            return this;
        }

        public StepAdder setRatio(double ratio) {
            this.ratio = ratio;
            return this;
        }

        public StepAdder setR(double r) {
            this.r = r;
            return this;
        }

        public StepAdder setX(double x) {
            this.x = x;
            return this;
        }

        public StepAdder setG1(double g1) {
            this.g1 = g1;
            return this;
        }

        public StepAdder setB1(double b1) {
            this.b1 = b1;
            return this;
        }

        public StepAdder setG2(double g2) {
            this.g2 = g2;
            return this;
        }

        public StepAdder setB2(double b2) {
            this.b2 = b2;
            return this;
        }

        public TapChanger endStep() {
            steps.add(this);
            return TapChanger.this;
        }

        public double getAngle() {
            return angleRad;
        }

        public double getRatio() {
            return ratio;
        }

        public double getR() {
            return r;
        }

        public double getX() {
            return x;
        }

        public double getG1() {
            return g1;
        }

        public double getB1() {
            return b1;
        }

        public double getG2() {
            return g2;
        }

        public double getB2() {
            return b2;
        }
    }

    public TapChanger setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    public TapChanger setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    public TapChanger setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        return this;
    }

    public TapChanger setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    public TapChanger setRegulationMode(PhaseTapChanger.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    public TapChanger setRegulationValue(double regulationValue) {
        this.regulationValue = regulationValue;
        return this;
    }

    public TapChanger setRegulationTerminal(String regulationTerminal) {
        this.regulationTerminal = regulationTerminal;
        return this;
    }

    public StepAdder beginStep() {
        return new StepAdder();
    }

    public int getLowTapPosition() {
        return lowTapPosition;
    }

    public Integer getTapPosition() {
        return tapPosition;
    }

    public List<StepAdder> getSteps() {
        return steps;
    }

    public boolean isLoadTapChangingCapabilities() {
        return loadTapChangingCapabilities;
    }

    public boolean isRegulating() {
        return regulating;
    }

    public PhaseTapChanger.RegulationMode getRegulationMode() {
        return regulationMode;
    }

    public double getRegulationValue() {
        return regulationValue;
    }

    public String getRegulationTerminal() {
        return regulationTerminal;
    }
}
