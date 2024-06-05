package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * An AreaBoundary is a boundary of an <code>Area</code>.
 * <p> It is composed of a terminal or a dangling line, associated with a boolean telling if it is an AC or DC boundary.
 * <p> To create and add an AreaBoundary, see {@link AreaAdder#addAreaBoundary(Terminal, boolean)} or {@link AreaAdder#addAreaBoundary(DanglingLine, boolean)}
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
 *             <td style="border: 1px solid black">Terminal</td>
 *             <td style="border: 1px solid black">Terminal</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Boundary of an Area/td>
 *         </tr>
*          <tr>
 *              <td style="border: 1px solid black">DanglingLine</td>
 *              <td style="border: 1px solid black">DanglingLine</td>
 *              <td style="border: 1px solid black"> - </td>
 *              <td style="border: 1px solid black">no</td>
 *              <td style="border: 1px solid black"> - </td>
 *              <td style="border: 1px solid black">Boundary of an Area</td>
 *         <tr>
 *             <td style="border: 1px solid black">AC</td>
 *             <td style="border: 1px solid black">boolean</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">True if the terminal correspond to an AC boundary, false otherwise</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 * @see Area
 */
public interface AreaBoundary {

    Optional<Terminal> getTerminal();

    Optional<DanglingLine> getDanglingLine();

    boolean isAc();

    double getP();

    double getQ();

}
