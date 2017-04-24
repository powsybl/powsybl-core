/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class TerminalTopologyVisitor extends AbstractTopologyVisitor {

    public abstract void visitTerminal(Terminal t);

    private void visitBranch(TwoTerminalsConnectable branch, TwoTerminalsConnectable.Side side) {
        switch (side) {
            case ONE:
                visitTerminal(branch.getTerminal1());
                break;
            case TWO:
                visitTerminal(branch.getTerminal2());
                break;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public void visitLine(Line line, TwoTerminalsConnectable.Side side) {
        visitBranch(line, side);
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoTerminalsConnectable.Side side) {
        visitBranch(transformer, side);
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
        switch (side) {
            case ONE:
                visitTerminal(transformer.getLeg1().getTerminal());
                break;
            case TWO:
                visitTerminal(transformer.getLeg2().getTerminal());
                break;
            case THREE:
                visitTerminal(transformer.getLeg3().getTerminal());
                break;
            default:
                throw new AssertionError();
        }
    }

    private void visitInjection(SingleTerminalConnectable injection) {
        visitTerminal(injection.getTerminal());
    }

    @Override
    public void visitGenerator(Generator generator) {
        visitInjection(generator);
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
    }
}
