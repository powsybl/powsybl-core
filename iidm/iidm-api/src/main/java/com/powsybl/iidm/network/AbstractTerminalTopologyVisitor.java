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
public abstract class AbstractTerminalTopologyVisitor extends DefaultTopologyVisitor {

    public abstract void visitTerminal(Terminal t);

    private void visitBranch(Branch branch, TwoSides side) {
        visitTerminal(branch.getTerminal(side));
    }

    @Override
    public void visitBusbarSection(BusbarSection busbarSection) {
        visitInjection(busbarSection);
    }

    @Override
    public void visitLine(Line line, TwoSides side) {
        visitBranch(line, side);
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side) {
        visitBranch(transformer, side);
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeSides side) {
        visitTerminal(transformer.getTerminal(side));
    }

    private void visitInjection(Injection injection) {
        visitTerminal(injection.getTerminal());
    }

    @Override
    public void visitGenerator(Generator generator) {
        visitInjection(generator);
    }

    @Override
    public void visitBattery(Battery battery) {
        visitInjection(battery);
    }

    @Override
    public void visitLoad(Load load) {
        visitInjection(load);
    }

    @Override
    public void visitShuntCompensator(ShuntCompensator sc) {
        visitInjection(sc);
    }

    @Override
    public void visitDanglingLine(DanglingLine danglingLine) {
        visitInjection(danglingLine);
    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
        visitInjection(staticVarCompensator);
    }

    @Override
    public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
        visitInjection(converterStation);
    }

    @Override
    public void visitGround(Ground ground) {
        visitInjection(ground);
    }

    @Override
    public void visitDcConverter(DcConverter<?> converter, TwoSides side) {
        visitTerminal(converter.getTerminal(side));
    }
}
