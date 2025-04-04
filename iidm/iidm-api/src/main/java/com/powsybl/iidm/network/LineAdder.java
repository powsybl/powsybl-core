/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * To create a new AC line, from a <code>Network</code> instance call the
 * {@link Network#newLine()} method to get a line builder instance.
 * <p>
 * Example:
 *<pre>
 *    Network n = ...
 *    Line l = n.newLine()
 *            .setId("l1")
 *            ...
 *        .add();
 *</pre>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @see Line
 * @see Network
 */
public interface LineAdder extends BranchAdder<Line, LineAdder> {

    static LineAdder fillLineAdder(LineAdder adder, Line line) {
        return adder.setR(line.getR())
                .setX(line.getX())
                .setG1(line.getG1())
                .setG2(line.getG2())
                .setB1(line.getB1())
                .setB2(line.getB2())
                .setVoltageLevel1(line.getTerminal1().getVoltageLevel().getId())
                .setVoltageLevel2(line.getTerminal2().getVoltageLevel().getId());
    }

    LineAdder setR(double r);

    LineAdder setX(double x);

    LineAdder setG1(double g1);

    LineAdder setB1(double b1);

    LineAdder setG2(double g2);

    LineAdder setB2(double b2);

    @Override
    Line add();

}
