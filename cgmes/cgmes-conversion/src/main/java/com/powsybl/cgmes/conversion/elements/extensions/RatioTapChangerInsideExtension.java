package com.powsybl.cgmes.conversion.elements.extensions;

import java.util.List;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.Terminal;

public class RatioTapChangerInsideExtension implements RatioTapChanger {

    private int lowTapPosition;
    private List<RatioTapChangerExtensionStep> steps;
    private int tapPosition;
    private boolean loadTapChangingCapabilities;
    private boolean regulating;
    private double targetV;
    private Terminal regulationTerminal;

    public RatioTapChangerInsideExtension(
            int lowTapPosition,
            List<RatioTapChangerExtensionStep> steps,
            Terminal regulationTerminal,
            boolean loadTapChangingCapabilities,
            int tapPosition,
            boolean regulating,
            double targetV) {
        this.lowTapPosition = lowTapPosition;
        this.steps = steps;
        this.regulationTerminal = regulationTerminal;
        this.tapPosition = tapPosition;
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        this.regulating = regulating;
        this.targetV = targetV;
    }

    @Override
    public int getLowTapPosition() {
        return lowTapPosition;
    }

    @Override
    public int getHighTapPosition() {
        return lowTapPosition + steps.size() - 1;
    }

    @Override
    public int getTapPosition() {
        return tapPosition;
    }

    @Override
    public RatioTapChanger setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    @Override
    public int getStepCount() {
        return steps.size();
    }

    @Override
    public RatioTapChangerStep getStep(int tapPosition) {
        return steps.get(tapPosition - lowTapPosition);
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        return getStep(tapPosition);
    }

    @Override
    public boolean isRegulating() {
        return regulating;
    }

    @Override
    public RatioTapChanger setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public Terminal getRegulationTerminal() {
        return regulationTerminal;
    }

    @Override
    public RatioTapChanger setRegulationTerminal(Terminal regulationTerminal) {
        this.regulationTerminal = regulationTerminal;
        return this;
    }

    @Override
    public void remove() {
        // Can not be removed
    }

    @Override
    public double getTargetV() {
        return targetV;
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return loadTapChangingCapabilities;
    }
}
