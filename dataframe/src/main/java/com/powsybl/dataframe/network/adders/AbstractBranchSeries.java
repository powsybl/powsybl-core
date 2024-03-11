/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.BranchAdder;
import com.powsybl.iidm.network.Network;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * Common series for all branches.
 *
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
abstract class AbstractBranchSeries extends IdentifiableSeries {

    protected final StringSeries voltageLevels1;
    protected final StringSeries connectableBuses1;
    protected final StringSeries buses1;
    protected final IntSeries nodes1;
    protected final StringSeries busOrBusbarSections1;
    protected final StringSeries voltageLevels2;
    protected final StringSeries connectableBuses2;
    protected final StringSeries buses2;
    protected final IntSeries nodes2;
    protected final StringSeries busOrBusbarSections2;

    AbstractBranchSeries(UpdatingDataframe dataframe) {
        super(dataframe);
        this.voltageLevels1 = dataframe.getStrings("voltage_level1_id");
        this.connectableBuses1 = dataframe.getStrings("connectable_bus1_id");
        this.buses1 = dataframe.getStrings("bus1_id");
        this.nodes1 = dataframe.getInts("node1");
        this.busOrBusbarSections1 = dataframe.getStrings("bus_or_busbar_section_id_1");
        this.voltageLevels2 = dataframe.getStrings("voltage_level2_id");
        this.connectableBuses2 = dataframe.getStrings("connectable_bus2_id");
        this.buses2 = dataframe.getStrings("bus2_id");
        this.nodes2 = dataframe.getInts("node2");
        this.busOrBusbarSections2 = dataframe.getStrings("bus_or_busbar_section_id_2");
    }

    protected void setBranchAttributes(BranchAdder<?, ?> adder, int row) {
        setIdentifiableAttributes(adder, row);
        applyIfPresent(voltageLevels1, row, adder::setVoltageLevel1);
        applyIfPresent(connectableBuses1, row, adder::setConnectableBus1);
        applyIfPresent(buses1, row, adder::setBus1);
        applyIfPresent(nodes1, row, adder::setNode1);
        applyIfPresent(voltageLevels2, row, adder::setVoltageLevel2);
        applyIfPresent(connectableBuses2, row, adder::setConnectableBus2);
        applyIfPresent(buses2, row, adder::setBus2);
        applyIfPresent(nodes2, row, adder::setNode2);
    }

    abstract BranchAdder create(Network network, int row);

}
