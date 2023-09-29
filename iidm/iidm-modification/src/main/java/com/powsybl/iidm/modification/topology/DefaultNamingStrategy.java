/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.*;

import java.util.List;

/**
 * Default naming strategy used if no other naming strategy is specified.
 * @author Nicolas Rol <nicolas.rol@rte-france.com>
 */
public class DefaultNamingStrategy implements NamingStrategy {

    private static final String SEPARATOR = "_";
    private static final String DISCONNECTOR_NAMEBASE = "DISCONNECTOR";
    private static final String BREAKER_NAMEBASE = "BREAKER";
    private static final String SWITCH_NAMEBASE = "SW";
    private static final String BUS_NAMEBASE = "BUS";

    @Override
    public final String getName() {
        return "Default";
    }

    @Override
    public String getSectioningPrefix(String prefixId, BusbarSection bbs, int busBarNum, int section1Num, int section2Num) {
        return prefixId;
    }

    @Override
    public String getChunkPrefix(String prefixId, List<SwitchKind> switchKindList, int busBarNum, int section1Num, int section2Num) {
        return prefixId;
    }

    private String getDisconnectorIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getDisconnectorId(String prefixId, int id1Num, int id2Num) {
        return prefixId + SEPARATOR + DISCONNECTOR_NAMEBASE + SEPARATOR + getDisconnectorIdSuffix(id1Num, id2Num);
    }

    @Override
    public String getDisconnectorId(BusbarSection bbs, String prefixId, int id1Num, int id2Num, boolean specifySide, int side) {
        return getDisconnectorId(prefixId, id1Num, id2Num);
    }

    @Override
    public String getDisconnectorBetweenChunksId(BusbarSection bbs, String prefixId, int id1Num, int id2Num) {
        return getDisconnectorId(prefixId, id1Num, id2Num);
    }

    public final String getBreakerIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getBreakerId(String prefixId) {
        return prefixId + SEPARATOR + BREAKER_NAMEBASE;
    }

    @Override
    public final String getBreakerId(String prefixId, int id1Num, int id2Num) {
        return prefixId + SEPARATOR + BREAKER_NAMEBASE + SEPARATOR + getBreakerIdSuffix(id1Num, id2Num);
    }

    private String getSwitchIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getSwitchId(String prefixId) {
        return prefixId + SEPARATOR + SWITCH_NAMEBASE;
    }

    @Override
    public final String getSwitchId(String prefixId, int idNum) {
        return prefixId + SEPARATOR + SWITCH_NAMEBASE + SEPARATOR + idNum;
    }

    @Override
    public final String getSwitchId(String prefixId, int id1Num, int id2Num) {
        return prefixId + SEPARATOR + SWITCH_NAMEBASE + SEPARATOR + getSwitchIdSuffix(id1Num, id2Num);
    }

    public final String getBusbarIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getBusbarId(String prefixId, int id1Num, int id2Num) {
        return prefixId + SEPARATOR + getBusbarIdSuffix(id1Num, id2Num);
    }

    @Override
    public final String getBusbarId(String prefixId, List<SwitchKind> switchKindList, int id1Num, int id2Num) {
        return prefixId + SEPARATOR + getBusbarIdSuffix(id1Num, id2Num);
    }

    @Override
    public final String getBusId(String prefixId) {
        return prefixId + SEPARATOR + BUS_NAMEBASE;
    }

    @Override
    public final String getBusId(String prefixId, String suffixId) {
        return prefixId + SEPARATOR + BUS_NAMEBASE + SEPARATOR + suffixId;
    }

    @Override
    public final String getBusId(String prefixId, int idNum) {
        return prefixId + SEPARATOR + BUS_NAMEBASE + SEPARATOR + idNum;
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
