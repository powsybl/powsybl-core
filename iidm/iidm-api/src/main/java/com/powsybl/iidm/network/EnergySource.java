/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * The energy source of a generator.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum EnergySource {
    HYDRO(false),
    NUCLEAR(false),
    WIND(true),
    THERMAL(false),
    SOLAR(true),
    OTHER(false);

    private final boolean intermittent;

    private EnergySource(boolean intermittent) {
        this.intermittent = intermittent;
    }

    public boolean isIntermittent() {
        return intermittent;
    }

}
