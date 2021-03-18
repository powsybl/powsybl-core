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
 * <p>
 *  Characteristics
 * </p>
 *
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *         <tr>
 *             <th style="border: 1px solid black">Attribute</th>
 *             <th style="border: 1px solid black">Type</th>
 *             <th style="border: 1px solid black">Unit</th>
 *             <th style="border: 1px solid black">Required</th>
 *             <th style="border: 1px solid black">Defaut value</th>
 *             <th style="border: 1px solid black">Description</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td style="border: 1px solid black">Id</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Unique identifier of the tie line</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the tie line</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">HalfLine1</td>
 *             <td style="border: 1px solid black">TieLine.HalfLine</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first half of the line characteristics</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">HalfLine2</td>
 *             <td style="border: 1px solid black">TieLine.HalfLine</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second half of the line characteristics</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">UcteXnodeCode</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The UCTE Xnode code corresponding to this line (only required if the line crosses a boundary)</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">R</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The series resistance (sum of the series resistances of the two Half lines) **NB: this attribute is read-only**</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">X</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The series reactance (sum of the series reactances of the two Hald lines) **NB: this attribute is read-only**</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">G1</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first side shunt conductance (sum of the first side shunt conductances of the two Half lines) **NB: this attribute is read-only**</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">B1</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first side shunt susceptance (sum of the first side shunt susceptances of the two Half lines) **NB: this attribute is read-only**</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">G2</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second side shunt conductance (sum of the second side shunt conductances of the two Half lines) **NB: this attribute is read-only**</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">B2</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second side shunt susceptance (sum of the second side shunt susceptances of the two Half lines) **NB: this attribute is read-only**</td>
 *         </tr>
 *     </tbody>
 * </table>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TieLine extends Line {

    /**
     * Half line
     *
     * <p>
     *  Characteristics
     * </p>
     *
     * <table style="border: 1px solid black; border-collapse: collapse">
     *     <thead>
     *         <tr>
     *             <th style="border: 1px solid black">Attribute</th>
     *             <th style="border: 1px solid black">Type</th>
     *             <th style="border: 1px solid black">Unit</th>
     *             <th style="border: 1px solid black">Required</th>
     *             <th style="border: 1px solid black">Defaut value</th>
     *             <th style="border: 1px solid black">Description</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td style="border: 1px solid black">Id</td>
     *             <td style="border: 1px solid black">String</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">Unique identifier of the half line</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">Name</td>
     *             <td style="border: 1px solid black">String</td>
     *             <td style="border: 1px solid black">-</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">Human-readable name of the half line</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">R</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">&Omega;</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The series resistance</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">X</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">&Omega;</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The series reactance</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">G1</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">S</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The first side shunt conductance</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">B1</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">S</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The first side shunt susceptance</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">G2</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">S</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The second side shunt conductance</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">B2</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">S</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The second side shunt susceptance</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">XNodeP</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">MW</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The active power consumption</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">XNodeQ</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">MVar</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The reactive power consumption</td>
     *         </tr>
     *     </tbody>
     * </table>
     */

    interface HalfLine extends LineCharacteristics<HalfLine> {

        String getId();

        String getName();

        /**
         * @deprecated Use {@link #getBoundary()} and {@link Boundary#getP()} instead.
         */
        @Deprecated
        default double getXnodeP() {
            return getBoundary().getP();
        }

        /**
         * @deprecated Boundary P is always calculated, never set.
         */
        @Deprecated
        default HalfLine setXnodeP(double p) {
            return this;
        }

        /**
         * @deprecated Use {@link #getBoundary()} and {@link Boundary#getQ()} instead.
         */
        @Deprecated
        default double getXnodeQ() {
            return getBoundary().getQ();
        }

        /**
         * @deprecated Boundary Q is always calculated, never set.
         */
        @Deprecated
        default HalfLine setXnodeQ(double q) {
            return this;
        }

        default Boundary getBoundary() {
            throw new UnsupportedOperationException();
        }

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
