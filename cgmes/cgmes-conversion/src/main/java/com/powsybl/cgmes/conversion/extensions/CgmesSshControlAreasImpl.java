/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class CgmesSshControlAreasImpl extends AbstractExtension<Network> implements CgmesSshControlAreas {

    private final List<ControlArea> controlAreas = new ArrayList<>();

    public CgmesSshControlAreasImpl(List<ControlArea> controlAreas) {
        this.controlAreas.addAll(Objects.requireNonNull(controlAreas));
    }

    @Override
    public List<ControlArea> getControlAreas() {
        return Collections.unmodifiableList(controlAreas);
    }

    public static class ControlArea {
        private String id;
        private double netInterchange;
        private double pTolerance;

        public ControlArea(String id, double netInterchange, double pTolerance) {
            this.id = id;
            this.netInterchange = netInterchange;
            this.pTolerance = pTolerance;
        }

        public String getId() {
            return id;
        }

        public double getNetInterchange() {
            return netInterchange;
        }

        public double getPTolerance() {
            return pTolerance;
        }
    }
}
