package com.powsybl.action.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.tasks.ModificationTask;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.Objects;

public class PhaseShifterDeltaTapTask implements ModificationTask {

    private final String phaseShifterId;
    private final int tapDelta;

    public PhaseShifterDeltaTapTask(String phaseShifterId, int tapDelta) {
        this.phaseShifterId = Objects.requireNonNull(phaseShifterId);
        this.tapDelta = tapDelta;
    }

    @Override
    public void modify(Network network, ComputationManager computationManager) {
        Objects.requireNonNull(network);
        TwoWindingsTransformer phaseShifter = network.getTwoWindingsTransformer(phaseShifterId);
        if (phaseShifter == null) {
            throw new PowsyblException("Transformer '" + phaseShifterId + "' not found");
        }
        if (phaseShifter.getPhaseTapChanger() == null) {
            throw new PowsyblException("Transformer '" + phaseShifterId + "' is not a phase shifter");
        }
        adjustTapPosition(phaseShifter);
        phaseShifter.getPhaseTapChanger().setRegulating(false);
        phaseShifter.getPhaseTapChanger().setRegulationMode(PhaseTapChanger.RegulationMode.DELTA_TAP);

    }

    private void adjustTapPosition(TwoWindingsTransformer phaseShifter) {
        if (tapDelta >= 0) {
            phaseShifter.getPhaseTapChanger().setTapPosition(
                    Math.min(phaseShifter.getPhaseTapChanger().getTapPosition() + tapDelta,
                            phaseShifter.getPhaseTapChanger().getHighTapPosition()));
        } else {
            phaseShifter.getPhaseTapChanger().setTapPosition(
                    Math.max(phaseShifter.getPhaseTapChanger().getTapPosition() + tapDelta,
                            phaseShifter.getPhaseTapChanger().getLowTapPosition()));
        }
    }
}
