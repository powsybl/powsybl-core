/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.TwoTerminalsConnectable.Side;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractTwoTerminalsConnectable<I extends Connectable<I>> extends ConnectableImpl<I> implements CurrentLimitsOwner<Side> {

    private CurrentLimits limits1;

    private CurrentLimits limits2;

    AbstractTwoTerminalsConnectable(String id, String name) {
        super(id, name);
    }

    public TerminalExt getTerminal1() {
        return terminals.get(0);
    }

    public TerminalExt getTerminal2() {
        return terminals.get(1);
    }

    public Terminal getTerminal(Side side) {
        switch (side) {
            case ONE:
                return getTerminal1();

            case TWO:
                return getTerminal2();

            default:
                throw new AssertionError();
        }
    }

    public Terminal getTerminal(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        boolean side1 = getTerminal1().getVoltageLevel().getId().equals(voltageLevelId);
        boolean side2 = getTerminal2().getVoltageLevel().getId().equals(voltageLevelId);
        if (side1) {
            return getTerminal1();
        } else if (side2) {
            return getTerminal2();
        } else if (side1 && side2) {
            throw new RuntimeException("Both terminals are connected to voltage level " + voltageLevelId);
        } else {
            throw new RuntimeException("No terminal connected to voltage level " + voltageLevelId);
        }
    }

    @Override
    public void setCurrentLimits(TwoTerminalsConnectable.Side side, CurrentLimitsImpl limits) {
        switch (side) {
            case ONE:
                limits1 = limits;
                break;
            case TWO:
                limits2 = limits;
                break;
            default:
                throw new InternalError();
        }
    }

    public CurrentLimits getCurrentLimits1() {
        return limits1;
    }

    public CurrentLimitsAdder newCurrentLimits1() {
        return new CurrentLimitsAdderImpl<>(TwoTerminalsConnectable.Side.ONE, this);
    }

    public CurrentLimits getCurrentLimits2() {
        return limits2;
    }

    public CurrentLimitsAdder newCurrentLimits2() {
        return new CurrentLimitsAdderImpl<>(TwoTerminalsConnectable.Side.TWO, this);
    }

}
