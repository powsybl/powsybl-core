/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A Three Windings Power Transformer.
 * <p>
 * The equivalent star model used is: <div>
 * <object data="doc-files/threeWindingsTransformer.svg" type="image/svg+xml">
 * </object> </div>
 * <p>
 * Usually side 1 is the primary (high voltage), side 2 is the secondary (medium
 * voltage) and side 3 the tertiary voltage (low voltage).
 * <p>
 * g1, g2, g3 and b1, b2, b3 unit is siemens.
 * <p>
 * r1, r2, r3, x1, x2 and x3 unit is ohm.
 * <p>
 * A Three Windings Transformer is connected to three voltage levels (side 1, side 2 and
 * side 3) that belong to the same substation. It has three identical legs. Each leg has
 * the same model of a Two Windings Power Transformer.
 * <p>
 * All three legs may have a Ratio Tap Changer and a Phase Tap Changer.
 * A warning is emitted if a leg has both Ratio and Phase Tap Changers.
 * <p>
 * Only one Tap Changer is allowed to be regulating on the equipment. An exception is thrown if
 * two or more regulating controls are enabled.
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
 *             <td style="border: 1px solid black">RatedU0</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The rated voltage at the star bus</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Leg1</td>
 *             <td style="border: 1px solid black">ThreeWindingsTransformer.Leg</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The leg at the primary side</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Leg2</td>
 *             <td style="border: 1px solid black">ThreeWindingsTransformer.Leg</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The leg at the secondary side</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Leg3</td>
 *             <td style="border: 1px solid black">ThreeWindingsTransformer.Leg</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The leg at the tertiary side</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>
 * To create a three windings transformer, see {@link ThreeWindingsTransformerAdder}
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @see RatioTapChanger
 * @see PhaseTapChanger
 * @see ThreeWindingsTransformerAdder
 */
public interface ThreeWindingsTransformer extends Connectable<ThreeWindingsTransformer> {

    /**
     * Transformer leg
     *
     * <p>
     *     Characteristics
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
     *             <td style="border: 1px solid black">Terminal</td>
     *             <td style="border: 1px solid black">Terminal</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The terminal the leg is connected to </td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">R</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">&Omega;</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The nominal series resistance specified at the voltage of the leg</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">X</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">&Omega;</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The nominal series reactance specified at the voltage of the leg</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">G</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">S</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The nominal magnetizing conductance specified at the voltage of the leg</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">B</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">S</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The nominal magnetizing susceptance specified at the voltage of the leg</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">RatedU</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">kV</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The rated voltage</td>
     *         </tr>
     *         <tr>
     *             <td style="border: 1px solid black">RatedS</td>
     *             <td style="border: 1px solid black">double</td>
     *             <td style="border: 1px solid black">MVA</td>
     *             <td style="border: 1px solid black">yes</td>
     *             <td style="border: 1px solid black"> - </td>
     *             <td style="border: 1px solid black">The normal apparent power</td>
     *         </tr>
     *     </tbody>
     * </table>
     */
    public interface Leg extends RatioTapChangerHolder, PhaseTapChangerHolder, FlowsLimitsHolder {

        /**
         * Get the terminal the leg is connected to.
         */
        Terminal getTerminal();

        /**
         * Get the nominal series resistance specified in ohm at the voltage of
         * the leg.
         */
        double getR();

        /**
         * Set the nominal series resistance specified in ohm at the voltage of
         * the leg.
         */
        Leg setR(double r);

        /**
         * Get the nominal series reactance specified in ohm at the voltage of
         * the leg.
         */
        double getX();

        /**
         * Set the nominal series reactance specified in ohm at the voltage of
         * the leg.
         */
        Leg setX(double x);

        /**
         * Get the nominal magnetizing conductance specified in S at the voltage
         * of the leg.
         */
        double getG();

        /**
         * Set the nominal magnetizing conductance specified in S at the voltage
         * of the leg.
         */
        Leg setG(double g);

        /**
         * Get the nominal magnetizing susceptance specified in S at the voltage
         * of the leg.
         */
        double getB();

        /**
         * Set the nominal magnetizing susceptance specified in S at the voltage
         * of the leg.
         */
        Leg setB(double b);

        /**
         * Get the rated voltage in kV.
         */
        double getRatedU();

        /**
         * Set the rated voltage in kV.
         */
        Leg setRatedU(double ratedU);

        /**
         * Get the normal apparent power rating in MVA (optional).
         */
        default double getRatedS() {
            throw new UnsupportedOperationException();
        }

        /**
         * Set the normal apparent power rating in MVA (optional).
         */
        default Leg setRatedS(double ratedS) {
            throw new UnsupportedOperationException();
        }

        /**
         * Get side of the leg on the three windings transformer
         */
        ThreeSides getSide();

        Optional<? extends LoadingLimits> getLimits(LimitType type);
    }

    Terminal getTerminal(ThreeSides side);

    /**
     * Get the side the terminal is connected to.
     */
    ThreeSides getSide(Terminal terminal);

    Optional<Substation> getSubstation();

    default Substation getNullableSubstation() {
        return getSubstation().orElse(null);
    }

    /**
     * Get the leg at the primary side.
     */
    Leg getLeg1();

    /**
     * Get the leg at the secondary side.
     */
    Leg getLeg2();

    /**
     * Get the leg at the tertiary side.
     */
    Leg getLeg3();

    default Leg getLeg(ThreeSides side) {
        switch (side) {
            case ONE:
                return getLeg1();
            case TWO:
                return getLeg2();
            case THREE:
                return getLeg3();
            default:
                throw new PowsyblException("Can't get leg from side. Unsupported Side \"" + side + "\"");
        }
    }

    /**
     * Return the legs of this transformer, in the natural order (leg1, leg2 and leg3)
     */
    default Stream<Leg> getLegStream() {
        return Stream.of(getLeg1(), getLeg2(), getLeg3());
    }

    /**
     * Return the legs of this transformer, in the natural order (leg1, leg2 and leg3)
     */
    default List<Leg> getLegs() {
        return Arrays.asList(getLeg1(), getLeg2(), getLeg3());
    }

    /**
     * Get the ratedU at the fictitious bus in kV (also used as nominal voltage)
     */
    double getRatedU0();

    /**
     * Set the rated voltage at the fictitious bus.
     */
    default ThreeWindingsTransformer setRatedU0(double ratedU0) {
        throw new UnsupportedOperationException();
    }

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.THREE_WINDINGS_TRANSFORMER;
    }

    /**
     * Only checks overloading for LimitType.Current and permanent limits
     */
    boolean isOverloaded();

    /**
     * Only checks overloading for LimitType.Current and permanent limits
     */
    boolean isOverloaded(double limitReductionValue);

    int getOverloadDuration();

    boolean checkPermanentLimit(ThreeSides side, double limitReductionValue, LimitType type);

    boolean checkPermanentLimit(ThreeSides side, LimitType type);

    boolean checkPermanentLimit1(double limitReductionValue, LimitType type);

    boolean checkPermanentLimit1(LimitType type);

    boolean checkPermanentLimit2(double limitReductionValue, LimitType type);

    boolean checkPermanentLimit2(LimitType type);

    boolean checkPermanentLimit3(double limitReductionValue, LimitType type);

    boolean checkPermanentLimit3(LimitType type);

    Overload checkTemporaryLimits(ThreeSides side, double limitReductionValue, LimitType type);

    Overload checkTemporaryLimits(ThreeSides side, LimitType type);

    Overload checkTemporaryLimits1(double limitReductionValue, LimitType type);

    Overload checkTemporaryLimits1(LimitType type);

    Overload checkTemporaryLimits2(double limitReductionValue, LimitType type);

    Overload checkTemporaryLimits2(LimitType type);

    Overload checkTemporaryLimits3(double limitReductionValue, LimitType type);

    Overload checkTemporaryLimits3(LimitType type);
}
