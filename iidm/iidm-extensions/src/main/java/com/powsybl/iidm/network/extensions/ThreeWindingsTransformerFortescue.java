/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.Objects;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ThreeWindingsTransformerFortescue extends Extension<ThreeWindingsTransformer> {

    String NAME = "threeWindingsTransformerFortescue";

    class LegFortescue {

        private double rz;
        private double xz;
        private boolean freeFluxes;
        private WindingConnectionType connectionType;
        private double groundingR;
        private double groundingX;

        public LegFortescue(double rz, double xz, boolean freeFluxes, WindingConnectionType connectionType,
                            double groundingR, double groundingX) {
            this.rz = rz;
            this.xz = xz;
            this.freeFluxes = freeFluxes;
            this.connectionType = Objects.requireNonNull(connectionType);
            this.groundingR = groundingR;
            this.groundingX = groundingX;
        }

        /**
         * Free fluxes set to true means that the magnetizing impedance Zm is infinite, i.e. fluxes are free.
         */
        public boolean isFreeFluxes() {
            return freeFluxes;
        }

        public void setFreeFluxes(boolean freeFluxes) {
            this.freeFluxes = freeFluxes;
        }

        /**
         * The zero sequence resistance of the leg.
         */
        public double getRz() {
            return rz;
        }

        public void setRz(double rz) {
            this.rz = rz;
        }

        /**
         * The zero sequence reactance of the leg.
         */
        public double getXz() {
            return xz;
        }

        public void setXz(double xz) {
            this.xz = xz;
        }

        /**
         * Get the winding connection type of the leg, see {@link WindingConnectionType}).
         */
        public WindingConnectionType getConnectionType() {
            return connectionType;
        }

        public void setConnectionType(WindingConnectionType connectionType) {
            this.connectionType = Objects.requireNonNull(connectionType);
        }

        /**
         * If the leg is earthed, depending on {@link WindingConnectionType}, it represents the
         * resistance part of the impedance to ground.
         */
        public double getGroundingR() {
            return groundingR;
        }

        public void setGroundingR(double groundingR) {
            this.groundingR = groundingR;
        }

        /**
         * If the leg is earthed, depending on {@link WindingConnectionType}, it represents the
         * reactance part of the impedance to ground.
         */
        public double getGroundingX() {
            return groundingX;
        }

        public void setGroundingX(double groundingX) {
            this.groundingX = groundingX;
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
