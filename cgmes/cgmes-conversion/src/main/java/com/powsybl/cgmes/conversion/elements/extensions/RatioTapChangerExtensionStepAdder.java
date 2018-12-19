package com.powsybl.cgmes.conversion.elements.extensions;

import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder.StepAdder;

class RatioTapChangerExtensionStepAdder implements StepAdder {

    private final RatioTapChangerExtensionAdder ratioTapChangerExtensionAdder;
    private double rho;
    private double r;
    private double x;
    private double g;
    private double b;

    RatioTapChangerExtensionStepAdder(RatioTapChangerExtensionAdder rtca) {
        this.ratioTapChangerExtensionAdder = rtca;
    }

    @Override
    public StepAdder setRho(double rho) {
        this.rho = rho;
        return this;
    }

    @Override
    public StepAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public StepAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public StepAdder setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public StepAdder setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public RatioTapChangerAdder endStep() {
        ratioTapChangerExtensionAdder.addStep(new RatioTapChangerExtensionStep(rho, r, x, g, b));
        return ratioTapChangerExtensionAdder;
    }
}