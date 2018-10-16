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

    interface TapAdder {

        TapAdder setPhaseShift(double phaseShift);

        TapAdder setRatio(double ratio);

        TapAdder setRdr(double rdr);

        TapAdder setRdx(double rdx);

        TapAdder setRdg(double rdg);

        TapAdder setRdb(double rdb);

        PhaseTapChangerAdder endTap();
    }

    PhaseTapChangerAdder setLowTapPosition(int lowTapPosition);

    PhaseTapChangerAdder setTapPosition(int tapPosition);

    PhaseTapChangerAdder setRegulating(boolean regulating);

    PhaseTapChangerAdder setRegulationMode(PhaseTapChanger.RegulationMode regulationMode);

    PhaseTapChangerAdder setRegulationValue(double regulationValue);

    PhaseTapChangerAdder setRegulationTerminal(Terminal regulationTerminal);

    TapAdder endTap();

    PhaseTapChanger add();
}
