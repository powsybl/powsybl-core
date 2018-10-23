/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * To create a dangling line, from a <code>VoltageLevel</code> instance call
 * the {@link VoltageLevel#newBoundaryLine()} method to get a dangling line
 * builder instance.
 * <p>
 * Example:
 *<pre>
 *    VoltageLevel vl = ...
 *    BoundaryLine dl = vl.newBoundaryLine()
 *            .setId("dl1")
 *            ...
 *        .add();
 *</pre>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see BoundaryLine
 * @see VoltageLevel
 */
public interface BoundaryLineAdder extends InjectionAdder<BoundaryLineAdder> {

    BoundaryLineAdder setP0(double p0);

    BoundaryLineAdder setQ0(double q0);

    BoundaryLineAdder setR(double r);

    BoundaryLineAdder setX(double x);

    BoundaryLineAdder setG(double g);

    BoundaryLineAdder setB(double b);

    BoundaryLineAdder setUcteXnodeCode(String ucteXnodeCode);

    BoundaryLine add();

}
