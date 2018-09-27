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
public interface RatioTapChangerAdder {

    public interface StepAdder {

        StepAdder setRatio(double rho);

        StepAdder setRdr(double rdr);

        StepAdder setRdx(double rdx);

        StepAdder setRdg(double rdg);

        StepAdder setRdb(double rdb);

        RatioTapChangerAdder endStep();
    }

    RatioTapChangerAdder setLowTapPosition(int lowTapPosition);

    RatioTapChangerAdder setTapPosition(int tapPosition);

    RatioTapChangerAdder setOnLoadTapChanger(boolean onLoadTapChanger);

    RatioTapChangerAdder setRegulating(boolean regulating);

    RatioTapChangerAdder setTargetV(double targetV);

    RatioTapChangerAdder setRegulationTerminal(Terminal regulationTerminal);

    StepAdder beginStep();

    RatioTapChanger add();

}
