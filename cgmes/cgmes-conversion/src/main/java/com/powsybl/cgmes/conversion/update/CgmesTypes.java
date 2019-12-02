/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.update;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 */
public enum CgmesTypes {
    SYNCHRONOUS_MACHINE("SynchronousMachine"),
    EXTERNAL_NETWORK_INJECTION("ExternalNetworkInjection"),
    ENERGY_CONSUMER("EnergyConsumer"),
    ASYNCHRONOUS_MACHINE("AsynchronousMachine"),
    RATIO_TAP_CHANGER("RatioTapChanger"),
    PHASE_TAP_CHANGER("PhaseTapChanger"),
    POWER_TRANSFORMER("PowerTransformer");

    CgmesTypes(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

    private final String type;
}
