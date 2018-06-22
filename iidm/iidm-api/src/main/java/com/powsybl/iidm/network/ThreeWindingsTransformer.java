/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A 3 windings power transformer.
 *<p>
 * The equivalent star model used is:
 * <div>
 *    <object data="doc-files/threeWindingsTransformer.svg" type="image/svg+xml">
 *    </object>
 * </div>
 * <p>Side 1 is the primary (high voltage), side 2 and side 3 can be indifferently
 * the secondary (medium voltage) or the tertiary voltage (low voltage).
 * <p>b1 and g1 unit is siemens.
 * <p>r1, r2, r3, x1, x2 and x3 unit is ohm.
 * <p>A 3 windings transformer is connected to 3 voltage levels (side 1, side 2 and side 3)
 * that belong to the same substation.
 * <p>To create a 3 windings transformer, see {@link ThreeWindingsTransformerAdder}
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see RatioTapChanger
 * @see PhaseTapChanger
 * @see ThreeWindingsTransformerAdder
 */
public interface ThreeWindingsTransformer extends Connectable<ThreeWindingsTransformer> {

    enum Side {
        ONE,
        TWO,
        THREE
    }

    interface LegBase<L extends LegBase> {

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
        L setR(double r);

        /**
         * Get the nominal series reactance specified in ohm at the voltage of
         * the leg.
         */
        double getX();

        /**
         * Set the nominal series reactance specified in ohm at the voltage of
         * the leg.
         */
        L setX(double x);

        /**
         * Get the rated voltage in kV.
         */
        double getRatedU();

        /**
         * Set the rated voltage in kV.
         */
        L setRatedU(double ratedU);

        CurrentLimits getCurrentLimits();

        CurrentLimitsAdder newCurrentLimits();

    }

    /**
     * Leg 1 of the equivalent star model.
     */
    interface Leg1 extends LegBase<Leg1> {

        /**
         * Get the nominal magnetizing conductance specified in S at the voltage
         * of the leg.
         */
        double getG();

        /**
         * Set the nominal magnetizing conductance specified in S at the voltage
         * of the leg.
         */
        Leg1 setG(double g);

        /**
         * Get the nominal magnetizing susceptance specified in S at the voltage
         * of the leg.
         */
        double getB();

        /**
         * Set the nominal magnetizing susceptance specified in S at the voltage
         * of the leg.
         */
        Leg1 setB(double b);

    }

    /**
     * Leg 2 or 3 of the equivalent star model.
     */
    interface Leg2or3 extends LegBase<Leg2or3>, RatioTapChangerHolder {

    }

    Terminal getTerminal(Side side);

    /**
     * Get the side the terminal is connected to.
     */
    Side getSide(Terminal terminal);

    /**
     * Get the substation to which the transformer belongs.
     */
    Substation getSubstation();

    /**
     * Get the leg at the primary side.
     */
    Leg1 getLeg1();

    /**
     * Get the leg at the secondary side.
     */
    Leg2or3 getLeg2();

    /**
     * Get the leg at the tertiary side.
     */
    Leg2or3 getLeg3();

}
