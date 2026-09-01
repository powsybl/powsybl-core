/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * DC nodes are points where DC terminals of DC conducting equipment are connected together with zero impedance.
 *
 * <p> To create a DcNode, see {@link DcNodeAdder}
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
 *             <td style="border: 1px solid black">Unique identifier of the DcNode</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the DcNode</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">NominalV</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The nominal voltage, always positive</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">LowVoltageLimit</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The low voltage limit, may be negative</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">HighVoltageLimit</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The high voltage limit, may be negative</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>
 * These limits apply to the signed value returned by {@link #getV()}, not to how big that number is once you
 * ignore its sign. The rule is always {@code LowVoltageLimit <= V <= HighVoltageLimit}. This matters because
 * the same check already written for {@link VoltageLevel} can be reused as is: a healthy DC Node on the
 * negative side reads something like {@code -500}. If the limits were stored ignoring the sign, as
 * {@code [480, 520]}, that check would see {@code -500 <= 480} and wrongly say the voltage is too low. Stored
 * signed instead, as {@code [-520, -480]}, the same check sees {@code -500} sits between them and reports
 * nothing, which is correct.
 * </p>
 *
 * <p>
 * A DC Node on the positive side has a band such as {@code [480, 520]}. One on the negative side has a band
 * such as {@code [-520, -480]}. On the negative side, going below {@code -520} (the low limit) means the
 * voltage is too big once you ignore the sign, and going above {@code -480} (the high limit) means it is too
 * small: the opposite of the positive side. So "low" always means the smaller number and "high" always means
 * the bigger number, but which one is "too big" and which one is "too little" flips depending on which side of
 * zero the band is on.
 * </p>
 *
 * <p>
 * A band can even sit around zero, such as {@code [-10, 10]}. There, {@code V = -15} crosses the low limit and
 * {@code V = 15} crosses the high limit, but both are the same problem: the voltage moved too far from zero, in
 * one direction or the other. Neither crossing means "too little", because every value close to zero is inside
 * the band and allowed. So code that wants to report "voltage too high" or "voltage too low" cannot decide it
 * from which field, low or high, was crossed. It first has to check where zero falls relative to the band.
 * </p>
 *
 * <p>
 * There are too many different bands for one meaning of "low" and "high" to fit them all: a positive-side node,
 * a negative-side node, and a node centered on zero each need a different answer. So this class does not try
 * to pick one. It only checks that {@code LowVoltageLimit <= HighVoltageLimit}
 * ({@link ValidationUtil#checkDcVoltageLimits}), the one rule that holds in every case, and leaves it to
 * whoever reads {@code V} and the band to decide what "too high" or "too low" means for that particular node.
 * </p>
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcNode extends Identifiable<DcNode>, DcTopologyVisitable {

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.DC_NODE;
    }

    /**
     * @return the nominal voltage in kV.
     */
    double getNominalV();

    /**
     * @param nominalV new nominal voltage in kV.
     * @return self for method chaining
     */
    DcNode setNominalV(double nominalV);

    /**
     * Get the low voltage limit in kV. The limit bounds the signed voltage {@link #getV()} and may be negative.
     *
     * @return the low voltage limit or NaN if undefined
     */
    double getLowVoltageLimit();

    /**
     * Set the low voltage limit in kV. The limit bounds the signed voltage {@link #getV()} and may be negative.
     *
     * @param lowVoltageLimit new low voltage limit in kV
     * @return self for method chaining
     */
    DcNode setLowVoltageLimit(double lowVoltageLimit);

    /**
     * Get the high voltage limit in kV. The limit bounds the signed voltage {@link #getV()} and may be negative.
     *
     * @return the high voltage limit or NaN if undefined
     */
    double getHighVoltageLimit();

    /**
     * Set the high voltage limit in kV. The limit bounds the signed voltage {@link #getV()} and may be negative.
     *
     * @param highVoltageLimit new high voltage limit in kV
     * @return self for method chaining
     */
    DcNode setHighVoltageLimit(double highVoltageLimit);

    /**
     * Get the voltage of the DC node in kV.
     */
    double getV();

    /**
     * Set the voltage of the DC node in kV.
     * @return self for method chaining
     */
    DcNode setV(double v);

    /**
     * Get the DcBus the DcNode is part of.
     */
    DcBus getDcBus();

    /**
     * remove the DcNode from the network
     */
    void remove();
}
