/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Branch.Side;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class TopologyVisitorAdapter extends AbstractAdapter<TopologyVisitor> implements TopologyVisitor {

    TopologyVisitorAdapter(TopologyVisitor delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public void visitBusbarSection(BusbarSection section) {
        getDelegate().visitBusbarSection(section);
    }

    @Override
    public void visitLine(Line line, Side side) {
        getDelegate().visitLine(line, side);
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Side side) {
        getDelegate().visitTwoWindingsTransformer(transformer, side);
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
        getDelegate().visitThreeWindingsTransformer(transformer, side);
    }

    @Override
    public void visitGenerator(Generator generator) {
        getDelegate().visitGenerator(generator);
    }

    @Override
    public void visitBattery(Battery battery) {
        getDelegate().visitBattery(battery);
    }

    @Override
    public void visitLoad(Load load) {
        getDelegate().visitLoad(load);
    }

    @Override
    public void visitShuntCompensator(ShuntCompensator sc) {
        getDelegate().visitShuntCompensator(sc);
    }

    @Override
    public void visitDanglingLine(DanglingLine danglingLine) {
        if (getIndex().isMerged(danglingLine)) {
            MergedLine line = getIndex().getMergedLineByCode(danglingLine.getUcteXnodeCode());
            Side side = line.getSide(danglingLine);
            getDelegate().visitLine(line, side);
        } else {
            getDelegate().visitDanglingLine(danglingLine);
        }
    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
        getDelegate().visitStaticVarCompensator(staticVarCompensator);
    }

    @Override
    public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
        getDelegate().visitHvdcConverterStation(converterStation);
    }
}
