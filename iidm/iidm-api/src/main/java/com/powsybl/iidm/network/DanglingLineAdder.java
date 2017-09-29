/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * To create a dangling line, from a <code>VoltageLevel</code> instance call
 * the {@link VoltageLevel#newDanglingLine()} method to get a dangling line
 * builder instance.
 * <p>
 * Example:
 *<pre>
 *    VoltageLevel vl = ...
 *    DanglingLine dl = vl.newDanglingLine()
 *            .setId("dl1")
 *            ...
 *        .add();
 *</pre>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see DanglingLine
 * @see VoltageLevel
 */
public interface DanglingLineAdder extends InjectionAdder<DanglingLineAdder> {

    DanglingLineAdder setP0(float p0);

    DanglingLineAdder setQ0(float q0);

    DanglingLineAdder setR(float r);

    DanglingLineAdder setX(float x);

    DanglingLineAdder setG(float g);

    DanglingLineAdder setB(float b);

    DanglingLineAdder setUcteXnodeCode(String ucteXnodeCode);

    DanglingLine add();

}
