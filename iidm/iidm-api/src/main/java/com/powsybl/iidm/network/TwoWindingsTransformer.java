/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * A two windings power transformer.
 * <p>The equivalent &#960; model used is:
 * <div>
 *    <object data="doc-files/twoWindingsTransformer.svg" type="image/svg+xml">
 *    </object>
 * </div>
 * <p>b, g, r, x shall be specified at the side 2 voltage.
 * <p>b and g unit is siemens, r and x unit is ohm.
 *
 * <p>
 *  Characteristics
 * </p>
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *         <tr>
 *             <th style="border: 1px solid black">Attribute</th>
 *             <th style="border: 1px solid black">Type</th>
 *             <th style="border: 1px solid black">Unit</th>
 *             <th style="border: 1px solid black">Required</th>
 *             <th style="border: 1px solid black">Default value</th>
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
 *             <td style="border: 1px solid black">Unique identifier of the transformer</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the transformer</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Rnom</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The nominal series resistance at the side 2 of the transformer </td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Xnom</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The nominal series reactance at the side 2 of the transformer</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Gnom</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The nominal magnetizing conductance at the side 2 of the transformer</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Bnom</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The nominal magnetizing susceptance at the side 2 of the transformer</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">V1nom</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The rated voltage at side 1</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">V2nom</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The rated voltage at side 2</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">RatedS</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black"> MVA </td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The normal apparent power</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>b, g, r, x, &#961; and &#945; variables in the model can be computed with
 * the following Java code supposing <code>transfo</code> is an instance of
 * <code>TwoWindingsTransformer</code>.
 * <pre>
 * r = transfo.getR()
 *    * (1 + (transfo.getRatioTapChanger() != null ? transfo.getRatioTapChanger().getCurrentStep().getR() / 100 : 0)
 *       + (transfo.getPhaseTapChanger() != null ? transfo.getPhaseTapChanger().getCurrentStep().getR() / 100 : 0));
 * x = transfo.getX()
 *    * (1 + (transfo.getRatioTapChanger() != null ? transfo.getRatioTapChanger().getCurrentStep().getX() / 100 : 0)
 *       + (transfo.getPhaseTapChanger() != null ? transfo.getPhaseTapChanger().getCurrentStep().getX() / 100 : 0));
 * g = transfo.getG()
 *    * (1 + (transfo.getRatioTapChanger() != null ? transfo.getRatioTapChanger().getCurrentStep().getG() / 100 : 0)
 *       + (transfo.getPhaseTapChanger() != null ? transfo.getPhaseTapChanger().getCurrentStep().getG() / 100 : 0));
 * b = transfo.getB()
 *    * (1 + (transfo.getRatioTapChanger() != null ? transfo.getRatioTapChanger().getCurrentStep().getB() / 100 : 0)
 *       + (transfo.getPhaseTapChanger() != null ? transfo.getPhaseTapChanger().getCurrentStep().getB() / 100 : 0));
 * rho = transfo.getRatedU2() / transfo.getRatedU1()
 *    * (transfo.getRatioTapChanger() != null ? transfo.getRatioTapChanger().getCurrentStep().getRho() : 1);
 *    * (transfo.getPhaseTapChanger() != null ? transfo.getPhaseTapChanger().getCurrentStep().getRho() : 1);
 * alpha = (transfo.getPhaseTapChanger() != null ? transfo.getPhaseTapChanger().getCurrentStep().getAlpha() : 0);
 *
 * </pre>
 * A 2 windings transformer is connected to 2 voltage levels (side 1 and side 2)
 * that belong to the same substation.
 * <p>To create a 2 windings transformer, see {@link TwoWindingsTransformerAdder}
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @see RatioTapChanger
 * @see PhaseTapChanger
 * @see TwoWindingsTransformerAdder
 */
public interface TwoWindingsTransformer extends Branch<TwoWindingsTransformer>, Connectable<TwoWindingsTransformer>, RatioTapChangerHolder, PhaseTapChangerHolder {

    Optional<Substation> getSubstation();

    default Substation getNullableSubstation() {
        return getSubstation().orElse(null);
    }

    /**
     * Get the nominal series resistance specified in &#937; at the secondary
     * voltage side.
     */
    double getR();

    /**
     * Set the nominal series resistance specified in &#937; at the secondary
     * voltage side.
     */
    TwoWindingsTransformer setR(double r);

    /**
     * Get the nominal series reactance specified in &#937; at the secondary
     * voltage side.
     */
    double getX();

    /**
     * Set the nominal series reactance specified in &#937; at the secondary
     * voltage side.
     */
    TwoWindingsTransformer setX(double x);

    /**
     * Get the nominal magnetizing conductance specified in S at the secondary
     * voltage side.
     */
    double getG();

    /**
     * Set the nominal magnetizing conductance specified in S at the secondary
     * voltage side.
     */
    TwoWindingsTransformer setG(double g);

    /**
     * Get the nominal magnetizing susceptance specified in S at the secondary
     * voltage side.
     */
    double getB();

    /**
     * Set the nominal magnetizing susceptance specified in S at the secondary
     * voltage side.
     */
    TwoWindingsTransformer setB(double b);

    /**
     * Get the primary winding rated voltage in kV.
     */
    double getRatedU1();

    /**
     * Set the secondary winding rated voltage in kV.
     */
    TwoWindingsTransformer setRatedU1(double ratedU1);

    /**
     * Get the secondary winding rated voltage in kV.
     */
    double getRatedU2();

    /**
     * Set the secondary winding rated voltage in kV.
     */
    TwoWindingsTransformer setRatedU2(double ratedU2);

    /**
     * Get the normal apparent power rating in MVA (optional).
     */
    default double getRatedS() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the normal apparent power rating in MVA (optional).
     */
    default TwoWindingsTransformer setRatedS(double ratedS) {
        throw new UnsupportedOperationException();
    }

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.TWO_WINDINGS_TRANSFORMER;
    }
}
