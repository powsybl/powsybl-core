/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.OptionalDouble;
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
 *             <td style="border: 1px solid black">interchangeTarget</td>
 *             <td style="border: 1px solid black">Double</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The optional target interchange of this area in MW, using load sign convention (negative is export, positive is import)</td>
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

    /**
     * Get the type of this area
     */
    String getAreaType();

    /**
     * Get all area voltage levels.
     */
    Iterable<VoltageLevel> getVoltageLevels();

    /**
     * Get all area voltage levels.
     */
    Stream<VoltageLevel> getVoltageLevelStream();

    /**
     * Adds a voltage level to the area.
     * @param voltageLevel voltage level to be added
     */
    Area addVoltageLevel(VoltageLevel voltageLevel);

    /**
     * Removes the provided VoltageLevel from the area. The VoltageLevel is not removed from the network,
     * the VoltageLevel is not part of the Area anymore.
     * @param voltageLevel the VoltageLevel to be removed from the Area.
     */
    Area removeVoltageLevel(VoltageLevel voltageLevel);

    /**
     * @return adder to create a new area boundary
     */
    AreaBoundaryAdder newAreaBoundary();

    /**
     * If exists, remove the area boundary associated with the provided terminal.
     * The Terminal and its Connectable are not removed from the network, but are not part of the Area anymore.
     * @param terminal terminal
     */
    Area removeAreaBoundary(Terminal terminal);

    /**
     * If exists, remove the area boundary associated with the provided DanglingLine's Boundary.
     * The DanglingLine and its Boundary are not removed from the network, but are not part of the Area anymore.
     * @param boundary DanglingLine's boundary
     */
    Area removeAreaBoundary(Boundary boundary);

    /**
     * If found, returns the area boundary associated with the provided DanglingLine's Boundary.
     * Otherwise, null is returned.
     * @param boundary DanglingLine's boundary
     */
    AreaBoundary getAreaBoundary(Boundary boundary);

    /**
     * If found, returns the area boundary associated with the provided Terminal.
     * Otherwise, null is returned.
     * @param terminal terminal
     */
    AreaBoundary getAreaBoundary(Terminal terminal);

    /**
     * Get all area boundaries.
     */
    Iterable<AreaBoundary> getAreaBoundaries();

    /**
     * Get all area boundaries.
     */
    Stream<AreaBoundary> getAreaBoundaryStream();

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.AREA;
    }

    /**
     * Get the optional target interchange of this area in MW, in load sign convention (negative is export, positive is import).
     * <p>Depends on the working variant.</p>
     *
     * @return the interchange target (MW)
     */
    OptionalDouble getInterchangeTarget();

    /**
     * Set the target interchange of this area in MW, in load sign convention (negative is export, positive is import).
     * Providing Double.NaN removes the target.
     * <p>Depends on the working variant.</p>
     * @param interchangeTarget new interchange target (MW)
     */
    Area setInterchangeTarget(double interchangeTarget);

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
    double getInterchange();

    void remove();

}
