/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TieLine extends Line {

    interface HalfLine extends LineCharacteristics<HalfLine> {

        String getId();

        String getName();

        /**
         * Get Xnode active power consumption in MW.
         */
        double getXnodeP();

        /**
         * Set Xnode active power consumption in MW.
         */
        HalfLine setXnodeP(double p);

        /**
         * Get Xnode reactive power consumption in MVar.
         */
        double getXnodeQ();

        /**
         * Set Xnode reactive power consumption in MVar.
         */
        HalfLine setXnodeQ(double q);

    }

    /**
     * Get the UCTE Xnode code corresponding to this line in the case where the
     * line is a boundary, return null otherwise.
     */
    String getUcteXnodeCode();

    /**
     * Get first half of the line characteristics
     */
    HalfLine getHalf1();

    /**
     * Get second half of the line characteristics
     */
    HalfLine getHalf2();

}
