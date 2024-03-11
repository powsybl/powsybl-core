/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.modification.topology.CreateBranchFeederBaysBuilder;
import com.powsybl.iidm.network.BranchAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

public abstract class AbstractFeederBaysSeries {

    abstract AbstractBranchSeries createTypedSeries(UpdatingDataframe dataframe);

    public CreateBranchFeederBaysBuilder createBuilder(Network n, UpdatingDataframe dataframe, int row) {
        AbstractBranchSeries series = createTypedSeries(dataframe);
        CreateBranchFeederBaysBuilder builder = new CreateBranchFeederBaysBuilder();
        BranchAdder<?, ?> lAdder = series.create(n, row);
        builder.withBranchAdder(lAdder);
        applyIfPresent(dataframe.getStrings("bus_or_busbar_section_id_1"), row, builder::withBusOrBusbarSectionId1);
        applyIfPresent(dataframe.getStrings("bus_or_busbar_section_id_2"), row, builder::withBusOrBusbarSectionId2);
        applyIfPresent(dataframe.getInts("position_order_1"), row, builder::withPositionOrder1);
        applyIfPresent(dataframe.getInts("position_order_2"), row, builder::withPositionOrder2);
        applyIfPresent(dataframe.getStrings("direction_1"), row, ConnectablePosition.Direction.class,
            builder::withDirection1);
        applyIfPresent(dataframe.getStrings("direction_2"), row, ConnectablePosition.Direction.class,
            builder::withDirection2);
        return builder;
    }
}
