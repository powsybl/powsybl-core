/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A tie line is an AC line sharing power between two neighbouring regional grids. It is constituted of two [half lines](#half-line).
 * A tie line is created by matching two [dangling lines](#dangling-line) with the same Xnode code.
 * It has line characteristics, with $$R$$ (resp. $$X$$) being the sum of the series resistances (resp. reactances) of the two half lines.
 * $$G1$$ (resp. $$B1$$) is equal to the sum of the first half line's $$G1$$ and $$G2$$ (resp. $$B1$$ and $$B2$$).
 * $$G2$$ (resp. $$B2$$) is equal to the sum of the second half line's $$G1$$ and $$G2$$ (resp. $$B1$$ and $$B2$$).
 *
 * **Characteristics**
 *
 * | Attribute | Type | Unit | Required | Default value | Description |
 * | --------- | ---- | ---- | -------- | ------------- | ----------- |
 * | Id | string | - | yes | - | Unique identifier of the tie line |
 * | Name | string | - | no | "" | Human-readable name of the tie line |
 * | HalfLine1 | `TieLine.HalfLine` | - | yes | - | The first half of the line characteristics |
 * | HalfLine2 | `TieLine.HalfLine` | - | yes | - | The second half of the line characteristics |
 * | UcteXnodeCode | String | - | no | - | The UCTE Xnode code corresponding to this line (only required if the line crosses a boundary) |
 * | R | double | $$\Omega\$$ | yes | - | The series resistance (sum of the series resistances of the two Half lines) **NB: this attribute is read-only** |
 * | X | double | $$\Omega\$$ | yes | - | The series reactance (sum of the series reactances of the two Hald lines) **NB: this attribute is read-only**  |
 * | G1 | double | S | yes | - | The first side shunt conductance (sum of the first side shunt conductances of the two Half lines) **NB: this attribute is read-only** |
 * | B1 | double | S | yes | - | The first side shunt susceptance (sum of the first side shunt susceptances of the two Half lines) **NB: this attribute is read-only**  |
 * | G2 | double | S | yes | - | The second side shunt conductance (sum of the second side shunt conductances of the two Half lines) **NB: this attribute is read-only** |
 * | B2 | double | S | yes | - | The second side shunt susceptance (sum of the second side shunt susceptances of the two Half lines) **NB: this attribute is read-only** |
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TieLine extends Line {

    /**
     * Half line
     *
     * **Characteristics**
     *
     * | Attribute | Type | Unit | Required | Default value | Description |
     * | --------- | ---- | ---- | -------- | ------------- | ----------- |
     * | Id | String | - | yes | - | The ID of the Half Line |
     * | Name | String | - | no | - | The name of the Half Line |
     * | R | double | $$\Omega\$$ | yes | - | The series resistance |
     * | X | double | $$\Omega\$$ | yes | - | The series reactance |
     * | G1 | double | S | yes | - | The first side shunt conductance |
     * | B1 | double | S | yes | - | The first side shunt susceptance |
     * | G2 | double | S | yes | - | The second side shunt conductance |
     * | B2 | double | S | yes | - | The second side shunt susceptance |
     * | XnodeP | double | MW | yes | - | The active power consumption |
     * | XnodeQ | double | MVar | yes | - | The reactive power consumption |
     */

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

        /**
         * Get the fictitious status
         */
        default boolean isFictitious() {
            return false;
        }

        /**
         * Set the fictitious status
         */
        default HalfLine setFictitious(boolean fictitious) {
            throw new UnsupportedOperationException();
        }

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

    /**
     * Get a given side half of the line characteristics
     */
    HalfLine getHalf(Side side);

}
