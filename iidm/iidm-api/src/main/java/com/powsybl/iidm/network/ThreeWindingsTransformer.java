/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

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
 * <p>
 * To create a three windings transformer, see {@link ThreeWindingsTransformerAdder}
 *
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

    public interface Leg extends RatioTapChangerHolder, PhaseTapChangerHolder {

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

        CurrentLimits getCurrentLimits();

        CurrentLimitsAdder newCurrentLimits();

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

        default double getP() {
            return getTerminal().getP();
        }

        default double getQ() {
            return getTerminal().getQ();
        }
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
    Leg getLeg1();

    /**
     * Get the leg at the secondary side.
     */
    Leg getLeg2();

    /**
     * Get the leg at the tertiary side.
     */
    Leg getLeg3();

    /**
     * Get the ratedU at the fictitious bus in kV (also used as nominal voltage)
     */
    double getRatedU0();
}
