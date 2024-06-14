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
 * <p> An Area is composed of a collection of voltage levels, and a collection of area boundaries.
 * <p> The area type is used to distinguish between various area concepts of different granularity.
 * For instance: control areas, bidding zones, countries...
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
 *             <td style="border: 1px solid black">The type of Area. For instance: "ControlArea", "BiddingZone", "Country"...</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">AcInterchangeTarget</td>
 *             <td style="border: 1px solid black">Double</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The optional target AC Interchange of this area in MW, using load sign convention (negative is export, positive is import)</td>
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

    void removeAreaBoundary(Boundary boundary);

    Iterable<AreaBoundary> getAreaBoundaries();

    Stream<AreaBoundary> getAreaBoundaryStream();

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.AREA;
    }

    /**
     * Get the optional target AC Interchange of this area in MW, in load sign convention (negative is export, positive is import)
     * @return the AC Interchange target (MW)
     */
    Optional<Double> getAcInterchangeTarget();

    /**
     * Get the current AC Interchange of this area in MW, in load sign convention (negative is export, positive is import)
     * @return the AC position (MW, 0 MW if no boundary)
     */
    double getAcInterchange();

    /**
     * Get the current DC Interchange of this area in MW, in load sign convention (negative is export, positive is import)
     * @return the DC position (MW, 0 MW if no boundary)
     */
    double getDcInterchange();

    /**
     * Get the current total (AC+DC) Interchange of this area in MW, in load sign convention (negative is export, positive is import)
     * @return the total position (MW, 0 MW if no boundary)
     */
    double getTotalInterchange();

    void removeVoltageLevel(VoltageLevel voltageLevel);

    void remove();

}
