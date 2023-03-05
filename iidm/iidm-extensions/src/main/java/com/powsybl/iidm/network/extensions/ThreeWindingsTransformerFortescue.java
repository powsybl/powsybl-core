/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.Objects;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ThreeWindingsTransformerFortescue extends Extension<ThreeWindingsTransformer> {

    String NAME = "threeWindingsTransformerShortCircuit";

    class LegFortescue {

        private double ro;
        private double xo;
        private boolean freeFluxes;
        private LegConnectionType connectionType;

        public LegFortescue(double ro, double xo, boolean freeFluxes, LegConnectionType connectionType) {
            this.ro = ro;
            this.xo = xo;
            this.freeFluxes = freeFluxes;
            this.connectionType = Objects.requireNonNull(connectionType);
        }

        public boolean isFreeFluxes() {
            return freeFluxes;
        }

        public void setFreeFluxes(boolean freeFluxes) {
            this.freeFluxes = freeFluxes;
        }

        public double getRo() {
            return ro;
        }

        public void setRo(double ro) {
            this.ro = ro;
        }

        public double getXo() {
            return xo;
        }

        public void setXo(double xo) {
            this.xo = xo;
        }

        public LegConnectionType getConnectionType() {
            return connectionType;
        }

        public void setConnectionType(LegConnectionType connectionType) {
            this.connectionType = Objects.requireNonNull(connectionType);
        }
    }

    @Override
    default String getName() {
        return NAME;
    }

    LegFortescue getLeg1();

    LegFortescue getLeg2();

    LegFortescue getLeg3();
}
