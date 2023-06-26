/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.network.adders.AbstractSimpleAdder;
import com.powsybl.dataframe.update.IntSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.dataframe.SideEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.fr>
 */
public class ConnectablePositionDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("feeder_name"),
        SeriesMetadata.ints("order"),
        SeriesMetadata.strings("direction"),
        SeriesMetadata.strings("side")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class ConnectablePositionSeries {
        private final StringSeries id;
        private final StringSeries feederName;
        private final IntSeries order;
        private final StringSeries direction;
        private final StringSeries side;

        ConnectablePositionSeries(UpdatingDataframe dataframe) {
            this.id = dataframe.getStrings("id");
            this.feederName = dataframe.getStrings("feeder_name");
            this.order = dataframe.getInts("order");
            this.direction = dataframe.getStrings("direction");
            this.side = dataframe.getStrings("side");
        }

        void createAdder(Network network, int row, Map<String, ConnectablePositionAdder> adderMap) {
            ConnectablePositionAdder adder;
            if (adderMap.containsKey(this.id.get(row))) {
                adder = adderMap.get(this.id.get(row));
            } else {
                Identifiable identifiable = network.getIdentifiable(this.id.get(row));
                Connectable connectable;
                if (identifiable instanceof Connectable) {
                    connectable = (Connectable) identifiable;
                } else {
                    throw new PowsyblException("id must be an id of a Connectable");
                }
                adder = (ConnectablePositionAdder) connectable.newExtension(ConnectablePositionAdder.class);
                adderMap.put(this.id.get(row), adder);
            }
            if (this.side == null || this.side.get(row).equals("")) {
                createFeeder(adder.newFeeder(), row);
            } else {
                switch (SideEnum.valueOf(this.side.get(row))) {
                    case ONE:
                        createFeeder(adder.newFeeder1(), row);
                        break;
                    case TWO:
                        createFeeder(adder.newFeeder2(), row);
                        break;
                    case THREE:
                        createFeeder(adder.newFeeder3(), row);
                        break;
                    default:
                        throw new PowsyblException("side must be ONE, TWO, THREE or None");
                }
            }
        }

        void createFeeder(ConnectablePositionAdder.FeederAdder feederAdder, int row) {
            feederAdder.withDirection(ConnectablePosition.Direction.valueOf(this.direction.get(row)))
                .withName(this.feederName.get(row))
                .withOrder(this.order.get(row))
                .add();
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        ConnectablePositionSeries series = new ConnectablePositionSeries(dataframe);
        Map<String, ConnectablePositionAdder> adderMap = new HashMap<>();
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.createAdder(network, row, adderMap);
        }
        adderMap.values().forEach(ExtensionAdder::add);
    }
}
