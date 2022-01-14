/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.util.DanglingLineBoundaryImpl;

import java.util.Optional;

/**
 * A dangling line to model boundaries (X nodes).
 * <p>A dangling line is a component that aggregates a line chunk and a constant
 * power injection (fixed p0, q0).
 * <div>
 *    <object data="doc-files/danglingLine.svg" type="image/svg+xml"></object>
 * </div>
 * Electrical characteritics (r, x, g, b) corresponding to a percent of the
 * orginal line.
 * <p>r, x, g, b have to be consistent with the declared length of the dangling
 * line.
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
 *             <td style="border: 1px solid black">Unique identifier of the dangling line</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the dangling line</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">P0</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MW</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The active power setpoint</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Q0</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MVar</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The reactive power setpoint</td>
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
 *             <td style="border: 1px solid black">G</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The shunt conductance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">B</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The shunt susceptance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">UcteXnodeCode</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The dangling line's UCTE Xnode code</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>To create a dangling line, see {@link DanglingLineAdder}
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 * @see DanglingLineAdder
 */
public interface DanglingLine extends Injection<DanglingLine>, FlowsLimitsHolder {

    interface Generation extends ReactiveLimitsHolder {
        /**
         * <p>Get the generator active power target in MW.</p>
         * <p>The active power target follows a generator sign convention.</p>
         * <p>Depends on the working variant.</p>
         * @return the generator active power target
         */
        double getTargetP();

        /**
         * <p>Set the generator active power target in MW.</p>
         * <p>Depends on the working variant.</p>
         * @param targetP the generator active power target
         * @return this to allow method chaining
         */
        Generation setTargetP(double targetP);

        /**
         * Get the generator maximal active power in MW.
         */
        double getMaxP();

        /**
         * Set the generator maximal active power in MW.
         */
        Generation setMaxP(double maxP);

        /**
         * Get the generator minimal active power in MW.
         */
        double getMinP();

        /**
         * Set the generator minimal active power in MW.
         */
        Generation setMinP(double minP);

        /**
         * <p>Get the generator reactive power target in MVAR.</p>
         * <p>The generator reactive power target follows a generator sign convention.</p>
         * <p>Depends on the working variant.</p>
         * @return the generator reactive power target
         */
        double getTargetQ();

        /**
         * <p>Set the generator reactive power target in MVAR.</p>
         * <p>Depends on the working variant.</p>
         * @param targetQ the generator reactive power target
         * @return this to allow method chaining
         */
        Generation setTargetQ(double targetQ);

        /**
         * Get the generator voltage regulation status.
         */
        boolean isVoltageRegulationOn();

        /**
         * Get the generator voltage regulation status as an optional if it is defined.
         * If not, return an empty optional.
         */
        default Optional<Boolean> findVoltageRegulationStatus() {
            return Optional.of(isVoltageRegulationOn());
        }

        /**
         * Set the generator voltage regulation status.
         */
        Generation setVoltageRegulationOn(boolean voltageRegulationOn);

        /**
         * <p>Get the generator voltage target in Kv.</p>
         * <p>Depends on the working variant.</p>
         * @return the generator voltage target
         */
        double getTargetV();

        /**
         * <p>Set the generator voltage target in Kv.</p>
         * <p>Depends on the working variant.</p>
         * @param targetV the generator voltage target
         * @return this to allow method chaining
         */
        Generation setTargetV(double targetV);
    }

    /**
     * Get the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getP0();

    /**
     * Set the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    DanglingLine setP0(double p0);

    /**
     * Get the constant reactive power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getQ0();

    /**
     * Set the constant reactive power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    DanglingLine setQ0(double q0);

    /**
     * Get the series resistance in &#937;.
     */
    double getR();

    /**
     * Set the series resistance in &#937;.
     */
    DanglingLine setR(double r);

    /**
     * Get the series reactance in &#937;.
     */
    double getX();

    /**
     * Set the series reactance in &#937;.
     */
    DanglingLine setX(double x);

    /**
     * Get the shunt conductance in S.
     */
    double getG();

    /**
     * Set the shunt conductance in S.
     */
    DanglingLine setG(double g);

    /**
     * Get the shunt susceptance in S.
     */
    double getB();

    /**
     * Set the shunt susceptance in S.
     */
    DanglingLine setB(double b);

    default Generation getGeneration() {
        return null;
    }

    /**
     * Get the UCTE Xnode code corresponding to this dangling line in the case
     * where the line is a boundary, return null otherwise.
     */
    String getUcteXnodeCode();

    default Boundary getBoundary() {
        return new DanglingLineBoundaryImpl(this);
    }

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.DANGLING_LINE;
    }
}
