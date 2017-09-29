/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractEquipmentTopologyVisitor extends DefaultTopologyVisitor {

    public abstract <I extends Connectable<I>> void visitEquipment(Connectable<I> eq);

    @Override
    public void visitLine(Line line, Branch.Side side) {
        visitEquipment(line);
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Branch.Side side) {
        visitEquipment(transformer);
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
        visitEquipment(transformer);
    }

    @Override
    public void visitGenerator(Generator generator) {
        visitEquipment(generator);
    }

    @Override
    public void visitLoad(Load load) {
        visitEquipment(load);
    }

    @Override
    public void visitShuntCompensator(ShuntCompensator sc) {
        visitEquipment(sc);
    }

    @Override
    public void visitDanglingLine(DanglingLine danglingLine) {
        visitEquipment(danglingLine);
    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
        visitEquipment(staticVarCompensator);
    }

    @Override
    public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
        visitEquipment(converterStation);
    }
}
