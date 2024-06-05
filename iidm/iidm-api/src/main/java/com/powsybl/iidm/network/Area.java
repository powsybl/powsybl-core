/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * An Area is a geographical zone of a given type.
 * <p> It is composed of a collection of voltage levels, and a collection of area boundaries.
 * <p> Areas can represent different granularities depending on their types. For instance: control areas, bidding zones, countries...
 * <p> To create an Area, see {@link AreaAdder}
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
 *             <td style="border: 1px solid black">Unique identifier of the Area</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the Area</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">AreaType</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The type of zone that this Area represents. For instance: "ControlArea", "BiddingZone", "Country"...</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">AcNetInterchangeTarget</td>
 *             <td style="border: 1px solid black">Double</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The optional target AC Net Interchange of this area in MW, using load sign convention</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 * @see VoltageLevel
 * @see AreaBoundary
 * @see AreaAdder
 */
public interface Area extends Identifiable<Area> {

    String getAreaType();

    Iterable<VoltageLevel> getVoltageLevels();

    Stream<VoltageLevel> getVoltageLevelStream();

    void addVoltageLevel(VoltageLevel voltageLevel);

    AreaBoundaryAdder newAreaBoundary();

    void removeAreaBoundary(Terminal terminal);

    void removeAreaBoundary(DanglingLine danglingLine);

    Iterable<AreaBoundary> getAreaBoundaries();

    Stream<AreaBoundary> getAreaBoundaryStream();

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.AREA;
    }

    /**
     * Get the optional target AC Net Interchange of this area in MW, using load sign convention
     * @return the AC Net Interchange target
     */
    Optional<Double> getAcNetInterchangeTarget();

    /**
     * Get the current AC Net Interchange of this area in MW, using load sign convention
     * @return the AC net position
     */
    Double getAcNetInterchange();

    /**
     * Get the current DC Net Interchange of this area in MW, using load sign convention
     * @return the DC net position
     */
    Double getDcNetInterchange();

    /**
     * Get the current total (AC+DC) Net Interchange of this area in MW, using load sign convention
     * @return the total net position
     */
    Double getTotalNetInterchange();

    void removeVoltageLevel(VoltageLevel voltageLevel);

}
