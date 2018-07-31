/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * An equipment with two terminals.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Branch<I extends Branch<I>> extends Connectable<I> {

    enum Side {
        ONE,
        TWO
    }

    interface Overload {

        CurrentLimits.TemporaryLimit getTemporaryLimit();

        double getPreviousLimit();

        String getPreviousLimitName();
    }

    /**
     * Get the first terminal.
     */
    Terminal getTerminal1();

    /**
     * Get the second terminal.
     */
    Terminal getTerminal2();

    Terminal getTerminal(Side side);

    Terminal getTerminal(String voltageLevelId);

    Side getSide(Terminal terminal);

    CurrentLimits getCurrentLimits(Side side);

    CurrentLimits getCurrentLimits1();

    CurrentLimitsAdder newCurrentLimits1();

    CurrentLimits getCurrentLimits2();

    CurrentLimitsAdder newCurrentLimits2();

    boolean isOverloaded();

    boolean isOverloaded(float limitReduction);

    int getOverloadDuration();

    boolean checkPermanentLimit(Side side, float limitReduction);

    boolean checkPermanentLimit(Side side);

    boolean checkPermanentLimit1(float limitReduction);

    boolean checkPermanentLimit1();

    boolean checkPermanentLimit2(float limitReduction);

    boolean checkPermanentLimit2();

    Overload checkTemporaryLimits(Side side, float limitReduction);

    Overload checkTemporaryLimits(Side side);

    Overload checkTemporaryLimits1(float limitReduction);

    Overload checkTemporaryLimits1();

    Overload checkTemporaryLimits2(float limitReduction);

    Overload checkTemporaryLimits2();
}
