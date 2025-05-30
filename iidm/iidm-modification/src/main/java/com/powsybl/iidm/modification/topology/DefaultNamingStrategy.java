/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.*;

import java.util.List;

/**
 * Default naming strategy used if no other naming strategy is specified.
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
@AutoService(NamingStrategy.class)
public class DefaultNamingStrategy implements NamingStrategy {

    private static final String SEPARATOR = NamingStrategyConstants.SEPARATOR;
    private static final String DISCONNECTOR_NAMEBASE = NamingStrategyConstants.DISCONNECTOR_NAMEBASE;
    private static final String BREAKER_NAMEBASE = NamingStrategyConstants.BREAKER_NAMEBASE;
    private static final String SWITCH_NAMEBASE = NamingStrategyConstants.SWITCH_NAMEBASE;
    private static final String BUS_NAMEBASE = NamingStrategyConstants.BUS_NAMEBASE;

    @Override
    public final String getName() {
        return NamingStrategyConstants.NAME;
    }

    @Override
    public final String getSectioningPrefix(String baseId, BusbarSection bbs, int busBarNum, int section1Num, int section2Num) {
        return baseId;
    }

    @Override
    public final String getChunkPrefix(String baseId, List<SwitchKind> switchKindList, int busBarNum, int section1Num, int section2Num) {
        return baseId;
    }

    @Override
    public final String getDisconnectorId(String baseId, int id1Num, int id2Num) {
        return baseId + SEPARATOR + DISCONNECTOR_NAMEBASE + SEPARATOR + id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getDisconnectorId(BusbarSection bbs, String baseId, int id1Num, int id2Num, int side) {
        return getDisconnectorId(baseId, id1Num, id2Num);
    }

    @Override
    public final String getDisconnectorBetweenChunksId(BusbarSection bbs, String baseId, int id1Num, int id2Num) {
        return getDisconnectorId(baseId, id1Num, id2Num);
    }

    @Override
    public final String getBreakerId(String baseId) {
        return baseId + SEPARATOR + BREAKER_NAMEBASE;
    }

    @Override
    public final String getBreakerId(String baseId, int id1Num, int id2Num) {
        return baseId + SEPARATOR + BREAKER_NAMEBASE + SEPARATOR + id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getSwitchId(String baseId) {
        return baseId + SEPARATOR + SWITCH_NAMEBASE;
    }

    @Override
    public final String getSwitchId(String baseId, int idNum) {
        return baseId + SEPARATOR + SWITCH_NAMEBASE + SEPARATOR + idNum;
    }

    @Override
    public final String getSwitchId(String baseId, int id1Num, int id2Num) {
        return baseId + SEPARATOR + SWITCH_NAMEBASE + SEPARATOR + id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getBusbarId(String baseId, int id1Num, int id2Num) {
        return baseId + SEPARATOR + id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getBusbarId(String baseId, List<SwitchKind> switchKindList, int id1Num, int id2Num) {
        return baseId + SEPARATOR + id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getBusId(String baseId) {
        return baseId + SEPARATOR + BUS_NAMEBASE;
    }

    @Override
    public final String getSwitchBaseId(Connectable<?> connectable, int side) {
        return connectable.getId() + (side == 0 ? "" : side);
    }

    @Override
    public final String getSwitchBaseId(VoltageLevel voltageLevel, BusbarSection bbs1, BusbarSection bbs2) {
        return voltageLevel.getId();
    }
}
