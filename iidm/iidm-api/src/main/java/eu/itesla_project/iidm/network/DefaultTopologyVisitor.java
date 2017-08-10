/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultTopologyVisitor implements TopologyVisitor {

    @Override
    public void visitBusbarSection(BusbarSection section) {
    }

    @Override
    public void visitLine(Line line, Line.Side side) {
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoWindingsTransformer.Side side) {
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
    }

    @Override
    public void visitGenerator(Generator generator) {
    }

    @Override
    public void visitLoad(Load load) {
    }

    @Override
    public void visitShuntCompensator(ShuntCompensator sc) {
    }

    @Override
    public void visitDanglingLine(DanglingLine danglingLine) {
    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
    }

    @Override
    public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
    }
}
