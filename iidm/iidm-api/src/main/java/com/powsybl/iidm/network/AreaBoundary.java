/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * An AreaBoundary is a boundary of an <code>Area</code>.
 * <p> It is composed of a terminal or a DanglingLine Boundary, associated with a boolean telling if it is an AC or DC boundary.
 * <p> To create and add an AreaBoundary, see {@link AreaAdder#addAreaBoundary(Terminal, boolean)} or {@link AreaAdder#addAreaBoundary(Boundary, boolean)}
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
 *             <td style="border: 1px solid black">Terminal</td>
 *             <td style="border: 1px solid black">Terminal</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Terminal at the border of an Area/td>
 *         </tr>
*          <tr>
 *              <td style="border: 1px solid black">Boundary</td>
 *              <td style="border: 1px solid black">Boundary</td>
 *              <td style="border: 1px solid black"> - </td>
 *              <td style="border: 1px solid black">no</td>
 *              <td style="border: 1px solid black"> - </td>
 *              <td style="border: 1px solid black">Boundary at the border of an Area</td>
 *         <tr>
 *             <td style="border: 1px solid black">AC</td>
 *             <td style="border: 1px solid black">boolean</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">True if this corresponds to an AC AreaBoundary, false otherwise</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 * @see Area
 * @see Boundary
 */
public interface AreaBoundary {

    /**
     * @return the Area this AreaBoundary belongs to
     */
    Area getArea();

    /**
     * @return If the boundary is defined by a Terminal, the Terminal of this AreaBoundary
     */
    Optional<Terminal> getTerminal();

    /**
     * @return If the boundary is defined by a DanglingLine's Boundary, the DanglingLine's Boundary of this AreaBoundary
     */
    Optional<Boundary> getBoundary();

    /**
     * @return true is the boundary was defined as AC
     */
    boolean isAc();

    /**
     * @return active power through area boundary
     */
    double getP();

    /**
     * @return reactive power through area boundary
     */
    double getQ();

}
