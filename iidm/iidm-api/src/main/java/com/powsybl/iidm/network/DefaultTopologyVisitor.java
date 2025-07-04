/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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
public class DefaultTopologyVisitor implements TopologyVisitor {

    @Override
    public void visitBusbarSection(BusbarSection section) {
        // empty default implementation
    }

    @Override
    public void visitLine(Line line, TwoSides side) {
        // empty default implementation
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side) {
        // empty default implementation
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeSides side) {
        // empty default implementation
    }

    @Override
    public void visitGenerator(Generator generator) {
        // empty default implementation
    }

    @Override
    public void visitBattery(Battery battery) {
        // empty default implementation
    }

    @Override
    public void visitLoad(Load load) {
        // empty default implementation
    }

    @Override
    public void visitShuntCompensator(ShuntCompensator sc) {
        // empty default implementation
    }

    @Override
    public void visitDanglingLine(DanglingLine danglingLine) {
        // empty default implementation
    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
        // empty default implementation
    }

    @Override
    public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
        // empty default implementation
    }

    @Override
    public void visitGround(Ground ground) {
        // empty default implementation
    }

    @Override
    public void visitAcDcConverter(AcDcConverter<?> converter, TwoSides side) {
        // empty default implementation
    }
}
