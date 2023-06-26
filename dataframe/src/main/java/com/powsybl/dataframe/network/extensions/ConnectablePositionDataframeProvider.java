/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.network.ExtensionInformation;
import com.powsybl.dataframe.network.NetworkDataframeMapper;
import com.powsybl.dataframe.network.NetworkDataframeMapperBuilder;
import com.powsybl.dataframe.network.adders.NetworkElementAdder;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.dataframe.NetworkUtil;
import com.powsybl.dataframe.SideEnum;

import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.fr>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class ConnectablePositionDataframeProvider extends AbstractSingleDataframeNetworkExtension {

    @Override
    public String getExtensionName() {
        return ConnectablePosition.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(ConnectablePosition.NAME,
            "it gives the position of a connectable relative to other equipments in the network",
            "index : id (str), side (str), order (int), feeder_name (str), direction (str)");
    }

    private ConnectablePositionFeederData getOrThrow(Network network, UpdatingDataframe dataframe, int index) {
        String id = dataframe.getStringValue("id", index)
            .orElseThrow(() -> new IllegalArgumentException("id column is missing"));
        String side = dataframe.getStringValue("side", index)
            .orElse("");
        Identifiable identifiable = network.getIdentifiable(id);
        Connectable connectable;
        if (identifiable instanceof Connectable) {
            connectable = (Connectable) identifiable;
        } else {
            throw new PowsyblException("id must be an id of a Connectable");
        }
        ConnectablePosition connectablePosition = (ConnectablePosition) connectable.getExtension(
            ConnectablePosition.class);
        if (connectablePosition == null) {
            throw new PowsyblException("Connectable '" + id + "' has no ConnectablePosition extension");
        }
        if (side.equals("")) {
            return new ConnectablePositionFeederData(id, connectablePosition.getFeeder(), null);
        }
        SideEnum sideEnum = SideEnum.valueOf(side);
        switch (sideEnum) {
            case ONE:
                return new ConnectablePositionFeederData(id, connectablePosition.getFeeder1(), sideEnum);
            case TWO:
                return new ConnectablePositionFeederData(id, connectablePosition.getFeeder2(), sideEnum);
            case THREE:
                return new ConnectablePositionFeederData(id, connectablePosition.getFeeder3(), sideEnum);
            default:
                throw new PowsyblException("side must be ONE, TWO, THREE or None");
        }
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(NetworkUtil::getFeeders, this::getOrThrow)
            .stringsIndex("id", ConnectablePositionFeederData::getId)
            .strings("side", feeder -> feeder.getSide() == null ? null : feeder.getSide().toString())
            .ints("order", ConnectablePositionFeederData::getOrder)
            .strings("feeder_name", ConnectablePositionFeederData::getFeederName)
            .strings("direction", ConnectablePositionFeederData::getDirection)
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(network::getIdentifiable)
            .filter(Objects::nonNull)
            .filter(identifiable -> identifiable instanceof Connectable)
            .forEach(connectable -> connectable.removeExtension(ConnectablePosition.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new ConnectablePositionDataframeAdder();
    }
}
