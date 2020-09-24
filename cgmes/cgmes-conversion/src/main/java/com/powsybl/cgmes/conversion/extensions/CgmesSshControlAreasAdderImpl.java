/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.cgmes.conversion.extensions.CgmesSshControlAreasImpl.ControlArea;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class CgmesSshControlAreasAdderImpl extends AbstractExtensionAdder<Network, CgmesSshControlAreas> implements CgmesSshControlAreasAdder {

    private final List<ControlArea> controlAreas = new ArrayList<>();

    public CgmesSshControlAreasAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public CgmesSshControlAreasAdder addControlArea(ControlArea controlArea) {
        this.controlAreas.add(Objects.requireNonNull(controlArea));
        return this;
    }

    @Override
    protected CgmesSshControlAreas createExtension(Network extendable) {
        if (controlAreas.isEmpty()) {
            throw new PowsyblException("cgmesSshControlAreas.controlAreas is empty");
        }
        return new CgmesSshControlAreasImpl(controlAreas);
    }
}
