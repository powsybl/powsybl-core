/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TopologyVisitor {

    void visitBusbarSection(BusbarSection section);

    void visitLine(Line line, Line.Side side);

    void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoWindingsTransformer.Side side);

    void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side);

    void visitGenerator(Generator generator);

    void visitLoad(Load load);

    void visitShuntCompensator(ShuntCompensator sc);

    void visitDanglingLine(DanglingLine danglingLine);

    void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator);

    default void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
    }
}
