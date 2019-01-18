/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.TapChanger;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableTapChanger {

    TapChanger tapChanger;

    ImmutableTapChanger(TapChanger tapChanger) {
        this.tapChanger = Objects.requireNonNull(tapChanger);
    }

    public int getLowTapPosition() {
        return tapChanger.getLowTapPosition();
    }

    public int getHighTapPosition() {
        return tapChanger.getHighTapPosition();
    }

    public int getTapPosition() {
        return tapChanger.getTapPosition();
    }

    public int getStepCount() {
        return tapChanger.getStepCount();
    }

    public boolean isRegulating() {
        return tapChanger.isRegulating();
    }

    public Terminal getRegulationTerminal() {
        return ImmutableTerminal.ofNullable(tapChanger.getRegulationTerminal());
    }

    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
