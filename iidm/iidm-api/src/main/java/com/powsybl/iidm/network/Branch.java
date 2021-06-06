/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Collections;

/**
 * An equipment with two terminals.
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
 *             <td style="border: 1px solid black">Unique identifier of the branch</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the branch</td>
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
 *     </tbody>
 * </table>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Branch<I extends Branch<I>> extends Connectable<I> {

    enum Side {
        ONE,
        TWO
    }

    /**
     * Represents a current overload on a {@link Branch}.
     */
    interface Overload {

        /**
         * The temporary limit under which the current is.
         * In particular, it determines the duration during which
         * the current current value may be sustained.
         */
        CurrentLimits.TemporaryLimit getTemporaryLimit();

        /**
         * The value of the current limit which has been overloaded, in Amperes.
         */
        double getPreviousLimit();

        /**
         * The name of the current limit which has been overloaded.
         */
        String getPreviousLimitName();
    }

    /**
     * Get the first terminal.
     */
    Terminal getTerminal1();

    /**
     * Get the second terminal.
     */
    Terminal getTerminal2();

    Terminal getTerminal(Side side);

    Terminal getTerminal(String voltageLevelId);

    Side getSide(Terminal terminal);

    default Collection<OperationalLimits> getOperationalLimits1() {
        return getCurrentLimits1() != null ? Collections.singletonList(getCurrentLimits1()) : Collections.emptyList();
    }

    CurrentLimits getCurrentLimits1();

    default ActivePowerLimits getActivePowerLimits1() {
        return null;
    }

    default ApparentPowerLimits getApparentPowerLimits1() {
        return null;
    }

    CurrentLimitsAdder newCurrentLimits1();

    ActivePowerLimitsAdder newActivePowerLimits1();

    ApparentPowerLimitsAdder newApparentPowerLimits1();

    default Collection<OperationalLimits> getOperationalLimits2() {
        return getCurrentLimits2() != null ? Collections.singletonList(getCurrentLimits2()) : Collections.emptyList();
    }

    CurrentLimits getCurrentLimits2();

    default ActivePowerLimits getActivePowerLimits2() {
        return null;
    }

    default ApparentPowerLimits getApparentPowerLimits2() {
        return null;
    }

    CurrentLimitsAdder newCurrentLimits2();

    ActivePowerLimitsAdder newActivePowerLimits2();

    ApparentPowerLimitsAdder newApparentPowerLimits2();

    default CurrentLimits getCurrentLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getCurrentLimits1();
        } else if (side == Branch.Side.TWO) {
            return getCurrentLimits2();
        }
        throw new AssertionError("Unexpected side: " + side);
    }

    default ActivePowerLimits getActivePowerLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getActivePowerLimits1();
        } else if (side == Branch.Side.TWO) {
            return getActivePowerLimits2();
        }
        throw new AssertionError("Unexpected side: " + side);
    }

    default ApparentPowerLimits getApparentPowerLimits(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return getApparentPowerLimits1();
        } else if (side == Branch.Side.TWO) {
            return getApparentPowerLimits2();
        }
        throw new AssertionError("Unexpected side: " + side);
    }

    default LoadingLimits getLimits(LimitType type, Branch.Side side) {
        switch (type) {
            case CURRENT:
                return getCurrentLimits(side);
            case ACTIVE_POWER:
                return getActivePowerLimits(side);
            case APPARENT_POWER:
                return getApparentPowerLimits(side);
            default:
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        }
    }

    /**
     * Only checks overloading for LimitType.Current and permanent limits
     */
    boolean isOverloaded();

    /**
     * Only checks overloading for LimitType.Current and permanent limits
     */
    boolean isOverloaded(float limitReduction);

    int getOverloadDuration();

    /**
     * @deprecated Since 4.3.0, use {@link #checkPermanentLimit(Side, float, LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default boolean checkPermanentLimit(Side side, float limitReduction) {
        return checkPermanentLimit(side, limitReduction, LimitType.CURRENT);
    }

    default boolean checkPermanentLimit(Side side, float limitReduction, LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkPermanentLimit(side, limitReduction);
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkPermanentLimit(Side, LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default boolean checkPermanentLimit(Side side) {
        return checkPermanentLimit(side, LimitType.CURRENT);
    }

    default boolean checkPermanentLimit(Side side, LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkPermanentLimit(side);
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkPermanentLimit1(float, LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default boolean checkPermanentLimit1(float limitReduction) {
        return checkPermanentLimit1(limitReduction, LimitType.CURRENT);
    }

    default boolean checkPermanentLimit1(float limitReduction, LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkPermanentLimit1(limitReduction);
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkPermanentLimit1(LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default boolean checkPermanentLimit1() {
        return checkPermanentLimit1(LimitType.CURRENT);
    }

    default boolean checkPermanentLimit1(LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkPermanentLimit1();
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkPermanentLimit2(float, LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default boolean checkPermanentLimit2(float limitReduction) {
        return checkPermanentLimit2(limitReduction, LimitType.CURRENT);
    }

    default boolean checkPermanentLimit2(float limitReduction, LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkPermanentLimit2(limitReduction);
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkPermanentLimit2(LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default boolean checkPermanentLimit2() {
        return checkPermanentLimit2(LimitType.CURRENT);
    }

    default boolean checkPermanentLimit2(LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkPermanentLimit2();
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkTemporaryLimits(Side, float, LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default Overload checkTemporaryLimits(Side side, float limitReduction) {
        return checkTemporaryLimits(side, limitReduction, LimitType.CURRENT);
    }

    default Overload checkTemporaryLimits(Side side, float limitReduction, LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkTemporaryLimits(side, limitReduction);
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkTemporaryLimits(Side, LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default Overload checkTemporaryLimits(Side side) {
        return checkTemporaryLimits(side, LimitType.CURRENT);
    }

    default Overload checkTemporaryLimits(Side side, LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkTemporaryLimits(side);
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkTemporaryLimits1(float, LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default Overload checkTemporaryLimits1(float limitReduction) {
        return checkTemporaryLimits1(limitReduction, LimitType.CURRENT);
    }

    default Overload checkTemporaryLimits1(float limitReduction, LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkTemporaryLimits1(limitReduction);
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkTemporaryLimits1(LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default Overload checkTemporaryLimits1() {
        return checkTemporaryLimits1(LimitType.CURRENT);
    }

    default Overload checkTemporaryLimits1(LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkTemporaryLimits1();
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkTemporaryLimits2(float, LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default Overload checkTemporaryLimits2(float limitReduction) {
        return checkTemporaryLimits2(limitReduction, LimitType.CURRENT);
    }

    default Overload checkTemporaryLimits2(float limitReduction, LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkTemporaryLimits2(limitReduction);
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }

    /**
     * @deprecated Since 4.3.0, use {@link #checkTemporaryLimits2(LimitType)} instead.
     */
    @Deprecated(since = "4.3.0")
    default Overload checkTemporaryLimits2() {
        return checkTemporaryLimits2(LimitType.CURRENT);
    }

    default Overload checkTemporaryLimits2(LimitType type) {
        if (type == LimitType.CURRENT) {
            return checkTemporaryLimits2();
        } else {
            throw new UnsupportedOperationException(
                    String.format("Limit type %s not supported in default implementation. Only %s is supported.", type.name(), LimitType.CURRENT));
        }
    }
}
