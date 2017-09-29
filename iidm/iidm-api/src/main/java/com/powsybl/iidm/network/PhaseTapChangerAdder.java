/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface PhaseTapChangerAdder {

    interface StepAdder {

        StepAdder setAlpha(float alpha);

        StepAdder setRho(float rho);

        StepAdder setR(float r);

        StepAdder setX(float x);

        StepAdder setG(float g);

        StepAdder setB(float b);

        PhaseTapChangerAdder endStep();
    }

    PhaseTapChangerAdder setLowTapPosition(int lowTapPosition);

    PhaseTapChangerAdder setTapPosition(int tapPosition);

    PhaseTapChangerAdder setRegulating(boolean regulating);

    PhaseTapChangerAdder setRegulationMode(PhaseTapChanger.RegulationMode regulationMode);

    PhaseTapChangerAdder setRegulationValue(float regulationValue);

    PhaseTapChangerAdder setRegulationTerminal(Terminal regulationTerminal);

    StepAdder beginStep();

    PhaseTapChanger add();
}
