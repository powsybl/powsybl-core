/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TopologyVisitor {

    void visitBusbarSection(BusbarSection section);

    void visitLine(Line line, TwoSides side);

    void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side);

    void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeSides side);

    void visitGenerator(Generator generator);

    default void visitBattery(Battery battery) {
        // Nothing to do
    }

    void visitLoad(Load load);

    void visitShuntCompensator(ShuntCompensator sc);

    void visitDanglingLine(DanglingLine danglingLine);

    void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator);

    default void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
    }

    void visitGround(Ground connectable);
}
