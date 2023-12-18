package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyVisitor;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.TwoWindingsTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 */
public class ConnectableTopologyVisitor implements TopologyVisitor {
    private final List<Connectable> connectables;

    public ConnectableTopologyVisitor() {
        this.connectables = new ArrayList<>();
    }

    @Override
    public void visitBusbarSection(BusbarSection busbarSection) {
        connectables.add(busbarSection);
    }

    @Override
    public void visitLine(Line line, TwoSides side) {
        connectables.add(line);
    }

    @Override
    public void visitTwoWindingsTransformer(TwoWindingsTransformer twoWindingsTransformer, TwoSides side) {
        connectables.add(twoWindingsTransformer);
    }

    @Override
    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeSides side) {

    }

    @Override
    public void visitGenerator(Generator generator) {
        connectables.add(generator);

    }

    @Override
    public void visitBattery(Battery battery) {
        TopologyVisitor.super.visitBattery(battery);
    }

    @Override
    public void visitLoad(Load load) {
        connectables.add(load);
    }

    @Override
    public void visitShuntCompensator(ShuntCompensator shuntCompensator) {
        connectables.add(shuntCompensator);
    }

    @Override
    public void visitDanglingLine(DanglingLine danglingLine) {
        connectables.add(danglingLine);

    }

    @Override
    public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
        connectables.add(staticVarCompensator);
    }

    @Override
    public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
        TopologyVisitor.super.visitHvdcConverterStation(converterStation);
    }

    public List<Connectable> getConnectables() {
        return connectables;
    }
}
