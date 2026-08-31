/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * A DC Line within a DC system. A DC Line connects two DC Nodes with a series resistance.
 *
 * <p> To create a DcLine, see {@link DcLineAdder}
 *
 * <p>
 *  Operational limits
 * </p>
 *
 * <p>A DC Line carries a single collection of {@link OperationalLimitsGroup} for the whole line, rather
 * than one collection per side as a {@link Branch} does. A DC Line has no shunt admittance, so the current
 * entering one DC Terminal is the current leaving the other, and one limit describes both.
 *
 * <p>{@link CurrentLimits} is the only kind of limit a DC Line accepts: it is the rating of the conductor,
 * and it is unambiguous because both DC Terminals carry the same current.
 *
 * <p>The other two kinds come with {@link FlowsLimitsHolder}, but a DC Line refuses them, because neither
 * describes anything physical on DC. An {@link ApparentPowerLimits} has no meaning where there is no
 * reactive power. An {@link ActivePowerLimits} would not say which DC Terminal it bounds, since the series
 * resistance makes the two carry different active power. Setting either one fails with a
 * {@link ValidationException}, and so does importing a network that contains one.
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
 *             <td style="border: 1px solid black">Unique identifier of the DcLine</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the DcLine</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">R</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> 0 </td>
 *             <td style="border: 1px solid black">DC Line series resistance</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcLine extends DcConnectable<DcLine>, FlowsLimitsHolder {

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.DC_LINE;
    }

    /**
     * @return the first DC Line Terminal
     */
    DcTerminal getDcTerminal1();

    /**
     * @return the second DC Line Terminal
     */
    DcTerminal getDcTerminal2();

    /**
     * @param side DC Line side
     * @return the DC Terminal at provided side
     */
    DcTerminal getDcTerminal(TwoSides side);

    /**
     * @param dcTerminal DC Terminal of the DC Line
     * @return the DC Line side of the provided DC Terminal
     */
    TwoSides getSide(DcTerminal dcTerminal);

    /**
     * @return the DC Line series resistance in &#937;.
     */
    double getR();

    /**
     * @param r the new DC Line series resistance in &#937;.
     * @return self for method chaining
     */
    DcLine setR(double r);
}
