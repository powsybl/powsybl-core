/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.dataframe.SideEnum;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.fr>
 */
public class ConnectablePositionFeederData {

    private final String id;
    private final String feederName;
    private final Integer order;
    private final String direction;
    private final SideEnum side;

    public ConnectablePositionFeederData(String id, String feederName, Integer order, String direction, SideEnum side) {
        this.id = id;
        this.feederName = feederName;
        this.order = order;
        this.direction = direction;
        this.side = side;
    }

    public ConnectablePositionFeederData(String id, ConnectablePosition.Feeder feeder, SideEnum side) {
        this(id, feeder.getName().orElse(null), feeder.getOrder().orElse(null), feeder.getDirection().toString(), side);
    }

    public String getId() {
        return id;
    }

    public String getFeederName() {
        return feederName;
    }

    public Integer getOrder() {
        return order;
    }

    public String getDirection() {
        return direction;
    }

    public SideEnum getSide() {
        return side;
    }
}
