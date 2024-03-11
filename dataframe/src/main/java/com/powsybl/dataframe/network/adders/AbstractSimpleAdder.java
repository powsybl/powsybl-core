/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.modification.topology.CreateFeederBay;
import com.powsybl.iidm.modification.topology.CreateFeederBayBuilder;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

import java.util.List;
import java.util.OptionalInt;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public abstract class AbstractSimpleAdder implements NetworkElementAdder {

    protected UpdatingDataframe getPrimaryDataframe(List<UpdatingDataframe> dataframes) {
        if (dataframes.size() != 1) {
            throw new IllegalArgumentException("Expected only one input dataframe");
        }
        return dataframes.get(0);
    }

    @FunctionalInterface
    public interface AdditionStrategy {
        void add(Network network, UpdatingDataframe df, InjectionAdder<?, ?> adder, int row, boolean throwException,
                 Reporter reporter);
    }

    public void addElementsWithBay(Network network, UpdatingDataframe dataframe, boolean throwException,
                                   Reporter reporter) {
        addElements(network, dataframe, this::addWithBay, throwException, reporter);
    }

    public void addElements(Network network, UpdatingDataframe dataframe) {
        addElements(network, dataframe, (n, df, adder, row, throwException, reporter) -> add(adder), false,
            Reporter.NO_OP);
    }

    void addElements(Network network, UpdatingDataframe dataframe, AdditionStrategy addition, boolean throwException,
                     Reporter reporter) {
        //do nothing
    }

    private void add(InjectionAdder<?, ?> injectionAdder) {
        if (injectionAdder instanceof LoadAdder loadAdder) {
            loadAdder.add();
        } else if (injectionAdder instanceof BatteryAdder batteryAdder) {
            batteryAdder.add();
        } else if (injectionAdder instanceof DanglingLineAdder danglingLineAdder) {
            danglingLineAdder.add();
        } else if (injectionAdder instanceof GeneratorAdder generatorAdder) {
            generatorAdder.add();
        } else if (injectionAdder instanceof ShuntCompensatorAdder shuntCompensatorAdder) {
            shuntCompensatorAdder.add();
        } else if (injectionAdder instanceof StaticVarCompensatorAdder staticVarCompensatorAdder) {
            staticVarCompensatorAdder.add();
        } else if (injectionAdder instanceof LccConverterStationAdder lccConverterStationAdder) {
            lccConverterStationAdder.add();
        } else if (injectionAdder instanceof VscConverterStationAdder vscConverterStationAdder) {
            vscConverterStationAdder.add();
        } else {
            throw new AssertionError("Given InjectionAdder not supported: " + injectionAdder.getClass().getName());
        }
    }

    private void addWithBay(Network network, UpdatingDataframe dataframe, InjectionAdder<?, ?> injectionAdder, int row,
                            boolean throwException, Reporter reporter) {
        String busOrBusbarSectionId = dataframe.getStrings("bus_or_busbar_section_id").get(row);
        OptionalInt injectionPositionOrder = dataframe.getIntValue("position_order", row);
        ConnectablePosition.Direction direction = ConnectablePosition.Direction.valueOf(
            dataframe.getStringValue("direction", row).orElse("BOTTOM"));
        CreateFeederBayBuilder builder = new CreateFeederBayBuilder()
            .withInjectionAdder(injectionAdder)
            .withBusOrBusbarSectionId(busOrBusbarSectionId)
            .withInjectionDirection(direction);
        if (injectionPositionOrder.isPresent()) {
            builder.withInjectionPositionOrder(injectionPositionOrder.getAsInt());
        }
        CreateFeederBay modification = builder.build();
        modification.apply(network, throwException, reporter == null ? Reporter.NO_OP : reporter);
    }

    @Override
    public void addElements(Network network, List<UpdatingDataframe> dataframes) {
        UpdatingDataframe primaryDf = getPrimaryDataframe(dataframes);
        addElements(network, primaryDf);
    }

    @Override
    public void addElementsWithBay(Network network, List<UpdatingDataframe> dataframes, boolean throwException,
                                   Reporter reporter) {
        UpdatingDataframe primaryDf = getPrimaryDataframe(dataframes);
        addElementsWithBay(network, primaryDf, throwException, reporter);
    }

}
