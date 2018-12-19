package com.powsybl.cgmes.conversion.elements.extensions;

import java.util.ArrayList;
import java.util.List;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;

class RatioTapChangerExtensionAdder implements RatioTapChangerAdder {

    private RatioTapChangerExtension ratioTapChangerExtension;

    private int lowTapPosition;
    private int tapPosition;
    private boolean loadTapChangingCapabilities;
    private boolean regulating;
    private double targetV;
    private Terminal regulationTerminal;
    private List<RatioTapChangerExtensionStep> steps = new ArrayList<>();

    RatioTapChangerExtensionAdder(RatioTapChangerExtension ratioTapChangerExtension) {
        this.ratioTapChangerExtension = ratioTapChangerExtension;
    }

    void addStep(RatioTapChangerExtensionStep step) {
        steps.add(step);
    }

    @Override
    public RatioTapChangerAdder setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    @Override
    public RatioTapChangerAdder setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        this.regulationTerminal = regulationTerminal;
        return this;
    }

    @Override
    public StepAdder beginStep() {
        return new RatioTapChangerExtensionStepAdder(this);
    }

    @Override
    public RatioTapChanger add() {
        RatioTapChanger rtc = new RatioTapChangerInsideExtension(
                lowTapPosition,
                steps,
                regulationTerminal,
                loadTapChangingCapabilities,
                tapPosition,
                regulating,
                targetV);
        ratioTapChangerExtension.setRatioTapChanger(rtc);
        return rtc;
    }
}