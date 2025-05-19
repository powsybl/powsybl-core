/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * A DC Switch within a DC system.
 *
 * todo ? add type enum breaker / disconnector ?
 *
 * <p> To create a DcSwitch, see {@link DcSwitchAdder}
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
 *             <td style="border: 1px solid black">Unique identifier of the DC Switch</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the DC Switch</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Open</td>
 *             <td style="border: 1px solid black">boolean</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Open status of the DC Switch</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Retained</td>
 *             <td style="border: 1px solid black">boolean</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Retain status of the DC Switch</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @see DcSwitchAdder
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcSwitch extends Identifiable<DcSwitch> {

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.DC_SWITCH;
    }

    /**
     * @return The DC node at side 1 of the DC Switch
     */
    DcNode getDcNode1();

    /**
     * @return The DC node at side 2 of the DC Switch
     */
    DcNode getDcNode2();

    /**
     * @return the retain status of the DC Switch
     */
    boolean isRetained();

    /**
     * @param retained new retain status of the DC Switch
     * @return self for method chaining
     */
    DcSwitch setRetained(boolean retained);

    /**
     * @return the open status of the DC Switch. Depends on the working variant.
     */
    boolean isOpen();

    /**
     * @param open new open status of the DC Switch. Depends on the working variant.
     * @return self for method chaining
     */
    DcSwitch setOpen(boolean open);

    /**
     * Remove the DC Switch from the network
     */
    void remove();
}
