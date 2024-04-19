/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;

/**
 * This test network is constituted the same as the one in FourSubstationsNodeBreakerFactory.
 * Only the position extensions for busbar sections and connectables are added.
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class FourSubstationsNodeBreakerWithExtensionsFactory {

    private FourSubstationsNodeBreakerWithExtensionsFactory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Network network = FourSubstationsNodeBreakerFactory.create(networkFactory);
        network.getBusbarSection("S1VL1_BBS").newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(1)
                .add();
        network.getBusbarSection("S1VL2_BBS1").newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(1)
                .add();
        network.getBusbarSection("S1VL2_BBS2").newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(2)
                .withSectionIndex(1)
                .add();
        network.getBusbarSection("S2VL1_BBS").newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(1)
                .add();
        network.getBusbarSection("S3VL1_BBS").newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(1)
                .add();
        network.getBusbarSection("S4VL1_BBS").newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(1)
                .add();

        network.getLoad("LD1").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("LD1")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(10)
                    .add()
                .add();
        network.getLoad("LD2").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("LD2")
                    .withDirection(ConnectablePosition.Direction.BOTTOM)
                    .withOrder(60)
                    .add()
                .add();
        network.getLoad("LD3").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("LD3")
                    .withDirection(ConnectablePosition.Direction.BOTTOM)
                    .withOrder(70)
                    .add()
                .add();
        network.getLoad("LD4").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("LD4")
                    .withDirection(ConnectablePosition.Direction.BOTTOM)
                    .withOrder(80)
                    .add()
                .add();
        network.getLoad("LD5").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("LD5")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(20)
                    .add()
                .add();
        network.getLoad("LD6").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("LD6")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(10)
                    .add()
                .add();

        network.getGenerator("GH1").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("GH1")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(30)
                    .add()
                .add();
        network.getGenerator("GH2").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("GH2")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(40)
                    .add()
                .add();
        network.getGenerator("GH3").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("GH3")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(50)
                    .add()
                .add();
        network.getGenerator("GTH1").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("GTH1")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(10)
                    .add()
                .add();
        network.getGenerator("GTH2").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("GTH2")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(30)
                    .add()
                .add();

        network.getShuntCompensator("SHUNT").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("SHUNT")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(90)
                    .add()
                .add();

        network.getStaticVarCompensator("SVC").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("SVC")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(20)
                    .add()
                .add();

        network.getVscConverterStation("VSC1").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("VSC1")
                    .withDirection(ConnectablePosition.Direction.BOTTOM)
                    .withOrder(20)
                    .add()
                .add();

        network.getVscConverterStation("VSC2").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                    .withName("VSC2")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(20)
                    .add()
                .add();

        network.getLccConverterStation("LCC1").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("LCC1")
                .withDirection(ConnectablePosition.Direction.BOTTOM)
                .withOrder(100)
                .add()
                .add();

        network.getLccConverterStation("LCC2").newExtension(ConnectablePositionAdder.class)
                .newFeeder()
                .withName("LCC2")
                .withDirection(ConnectablePosition.Direction.TOP)
                .withOrder(50)
                .add()
                .add();

        network.getTwoWindingsTransformer("TWT").newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                    .withName("TWT")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(20)
                    .add()
                .newFeeder2()
                    .withName("TWT")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(10)
                    .add()
                .add();

        network.getLine("LINE_S2S3").newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                    .withName("LINE_S2S3")
                    .withDirection(ConnectablePosition.Direction.BOTTOM)
                    .withOrder(30)
                    .add()
                .newFeeder2()
                    .withName("LINE_S2S3")
                    .withDirection(ConnectablePosition.Direction.BOTTOM)
                    .withOrder(10)
                    .add()
                .add();
        network.getLine("LINE_S3S4").newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                    .withName("LINE_S3S4")
                    .withDirection(ConnectablePosition.Direction.BOTTOM)
                    .withOrder(40)
                    .add()
                .newFeeder2()
                    .withName("LINE_S3S4")
                    .withDirection(ConnectablePosition.Direction.TOP)
                    .withOrder(30)
                    .add()
                .add();

        return network;
    }
}
