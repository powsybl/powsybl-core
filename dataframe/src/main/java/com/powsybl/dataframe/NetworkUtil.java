/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

import com.powsybl.dataframe.network.extensions.ConnectablePositionFeederData;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

import java.util.stream.Stream;

import static com.powsybl.dataframe.TemporaryLimitData.Side.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class NetworkUtil {

    private NetworkUtil() {
    }

    public static Stream<TemporaryLimitData> getLimits(Network network) {
        Stream.Builder<TemporaryLimitData> limits = Stream.builder();
        network.getBranchStream().forEach(branch -> {
            addLimit(limits, branch, (LoadingLimits) branch.getCurrentLimits1().orElse(null), ONE);
            addLimit(limits, branch, (LoadingLimits) branch.getCurrentLimits2().orElse(null), TWO);
            addLimit(limits, branch, (LoadingLimits) branch.getActivePowerLimits1().orElse(null), ONE);
            addLimit(limits, branch, (LoadingLimits) branch.getActivePowerLimits2().orElse(null), TWO);
            addLimit(limits, branch, (LoadingLimits) branch.getApparentPowerLimits1().orElse(null), ONE);
            addLimit(limits, branch, (LoadingLimits) branch.getApparentPowerLimits2().orElse(null), TWO);
        });
        network.getDanglingLineStream().forEach(danglingLine -> {
            addLimit(limits, danglingLine, danglingLine.getCurrentLimits().orElse(null), NONE);
            addLimit(limits, danglingLine, danglingLine.getActivePowerLimits().orElse(null), NONE);
            addLimit(limits, danglingLine, danglingLine.getApparentPowerLimits().orElse(null), NONE);
        });
        network.getThreeWindingsTransformerStream().forEach(threeWindingsTransformer -> {
            addLimit(limits, threeWindingsTransformer,
                threeWindingsTransformer.getLeg1().getCurrentLimits().orElse(null), ONE);
            addLimit(limits, threeWindingsTransformer,
                threeWindingsTransformer.getLeg1().getActivePowerLimits().orElse(null), ONE);
            addLimit(limits, threeWindingsTransformer,
                threeWindingsTransformer.getLeg1().getApparentPowerLimits().orElse(null), ONE);
            addLimit(limits, threeWindingsTransformer,
                threeWindingsTransformer.getLeg2().getCurrentLimits().orElse(null), TWO);
            addLimit(limits, threeWindingsTransformer,
                threeWindingsTransformer.getLeg2().getActivePowerLimits().orElse(null), TWO);
            addLimit(limits, threeWindingsTransformer,
                threeWindingsTransformer.getLeg2().getApparentPowerLimits().orElse(null), TWO);
            addLimit(limits, threeWindingsTransformer,
                threeWindingsTransformer.getLeg3().getCurrentLimits().orElse(null), THREE);
            addLimit(limits, threeWindingsTransformer,
                threeWindingsTransformer.getLeg3().getActivePowerLimits().orElse(null), THREE);
            addLimit(limits, threeWindingsTransformer,
                threeWindingsTransformer.getLeg3().getApparentPowerLimits().orElse(null), THREE);
        });
        return limits.build();
    }

    private static void addLimit(Stream.Builder<TemporaryLimitData> temporaryLimitContexts,
                                 Identifiable<?> identifiable,
                                 LoadingLimits limits, TemporaryLimitData.Side side) {
        if (limits != null) {
            temporaryLimitContexts.add(
                new TemporaryLimitData(identifiable.getId(), "permanent_limit", side, limits.getPermanentLimit(),
                    limits.getLimitType(), identifiable.getType()));
            limits.getTemporaryLimits().stream()
                .map(temporaryLimit -> new TemporaryLimitData(identifiable.getId(), temporaryLimit.getName(), side,
                    temporaryLimit.getValue(),
                    limits.getLimitType(), identifiable.getType(), temporaryLimit.getAcceptableDuration(),
                    temporaryLimit.isFictitious()))
                .forEach(temporaryLimitContexts::add);
        }
    }

    public static Stream<ConnectablePositionFeederData> getFeeders(Network network) {
        Stream.Builder<ConnectablePositionFeederData> feeders = Stream.builder();
        network.getConnectableStream().forEach(connectable -> {
            ConnectablePosition connectablePosition = (ConnectablePosition) connectable.getExtension(
                ConnectablePosition.class);
            if (connectablePosition != null) {
                if (connectablePosition.getFeeder() != null) {
                    feeders.add(
                        new ConnectablePositionFeederData(((Connectable) connectablePosition.getExtendable()).getId(),
                            connectablePosition.getFeeder(), null));
                }
                if (connectablePosition.getFeeder1() != null) {
                    feeders.add(
                        new ConnectablePositionFeederData(((Connectable) connectablePosition.getExtendable()).getId(),
                            connectablePosition.getFeeder1(), SideEnum.ONE));
                }
                if (connectablePosition.getFeeder2() != null) {
                    feeders.add(
                        new ConnectablePositionFeederData(((Connectable) connectablePosition.getExtendable()).getId(),
                            connectablePosition.getFeeder2(), SideEnum.TWO));
                }
                if (connectablePosition.getFeeder3() != null) {
                    feeders.add(
                        new ConnectablePositionFeederData(((Connectable) connectablePosition.getExtendable()).getId(),
                            connectablePosition.getFeeder3(), SideEnum.THREE));
                }
            }
        });
        return feeders.build();
    }

}
