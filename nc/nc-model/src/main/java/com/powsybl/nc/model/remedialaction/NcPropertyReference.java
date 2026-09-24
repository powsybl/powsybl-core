/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.remedialaction;

import java.util.Arrays;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public enum NcPropertyReference {
    SWITCH_OPEN("http://energy.referencedata.eu/PropertyReference/Switch.open"),
    SHUNT_COMPENSATOR_SECTIONS("http://energy.referencedata.eu/PropertyReference/ShuntCompensator.sections"),
    ROTATING_MACHINE_P("http://energy.referencedata.eu/PropertyReference/RotatingMachine.p"),
    TAP_CHANGER_STEP("http://energy.referencedata.eu/PropertyReference/TapChanger.step"),
    UNKNOWN(null);

    private final String uri;

    NcPropertyReference(String uri) {
        this.uri = uri;
    }

    public static NcPropertyReference fromUri(String uri) {
        return Arrays.stream(values())
            .filter(value -> value.uri != null && value.uri.equals(uri))
            .findFirst()
            .orElse(UNKNOWN);
    }
}
