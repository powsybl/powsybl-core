/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * Default naming strategy used if no other naming strategy is specified.
 * @author Nicolas Rol <nicolas.rol@rte-france.com>
 */
public class RteNamingStrategy implements NamingStrategy {

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
    public final String getDisconnectorIdPrefix(String prefixId) {
        return prefixId;
    }

    @Override
    public final String getDisconnectorIdSuffix(int idNum) {
        return String.valueOf(idNum);
    }

    @Override
    public final String getDisconnectorIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getDisconnectorId(String prefixId) {
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + DISCONNECTOR_NAMEBASE;
    }

    @Override
    public final String getDisconnectorId(String prefixId, int idNum) {
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + DISCONNECTOR_NAMEBASE + SEPARATOR + getDisconnectorIdSuffix(idNum);
    }

    @Override
    public final String getDisconnectorId(String prefixId, int id1Num, int id2Num) {
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + DISCONNECTOR_NAMEBASE + SEPARATOR + getDisconnectorIdSuffix(id1Num, id2Num);
    }

    @Override
    public final String getBreakerIdPrefix(String prefixId) {
        return prefixId;
    }

    @Override
    public final String getBreakerIdSuffix(int idNum) {
        return String.valueOf(idNum);
    }

    @Override
    public final String getBreakerIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getBreakerId(String prefixId) {
        return getBreakerIdPrefix(prefixId) + SEPARATOR + BREAKER_NAMEBASE;
    }

    @Override
    public final String getBreakerId(String prefixId, int idNum) {
        return getBreakerIdPrefix(prefixId) + SEPARATOR + BREAKER_NAMEBASE + SEPARATOR + getBreakerIdSuffix(idNum);
    }

    @Override
    public final String getBreakerId(String prefixId, int id1Num, int id2Num) {
        return getBreakerIdPrefix(prefixId) + SEPARATOR + BREAKER_NAMEBASE + SEPARATOR + getBreakerIdSuffix(id1Num, id2Num);
    }

    @Override
    public final String getSwitchIdPrefix(String prefixId) {
        return prefixId;
    }

    @Override
    public final String getSwitchIdSuffix(int idNum) {
        return String.valueOf(idNum);
    }

    @Override
    public final String getSwitchIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getSwitchId(String prefixId) {
        return getSwitchIdPrefix(prefixId) + SEPARATOR + SWITCH_NAMEBASE;
    }

    @Override
    public final String getSwitchId(String prefixId, int idNum) {
        return getSwitchIdPrefix(prefixId) + SEPARATOR + SWITCH_NAMEBASE + SEPARATOR + getSwitchIdSuffix(idNum);
    }

    @Override
    public final String getSwitchId(String prefixId, int id1Num, int id2Num) {
        return getSwitchIdPrefix(prefixId) + SEPARATOR + SWITCH_NAMEBASE + SEPARATOR + getSwitchIdSuffix(id1Num, id2Num);
    }

    @Override
    public final String getBusbarIdPrefix(String prefixId) {
        return prefixId;
    }

    @Override
    public final String getBusbarIdSuffix(int idNum) {
        return String.valueOf(idNum);
    }

    @Override
    public final String getBusbarIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public final String getBusbarId(String prefixId) {
        return getBusbarIdPrefix(prefixId);
    }

    @Override
    public final String getBusbarId(String prefixId, int idNum) {
        return getBusbarIdPrefix(prefixId) + SEPARATOR + getBusbarIdSuffix(idNum);
    }

    @Override
    public final String getBusbarId(String prefixId, int id1Num, int id2Num) {
        return getBusbarIdPrefix(prefixId) + SEPARATOR + getBusbarIdSuffix(id1Num, id2Num);
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
    public final String getFeederId(String prefixId, String voltageLevelId) {
        return prefixId + SEPARATOR + voltageLevelId;
    }

    @Override
    public final String getSwitchBaseId(Connectable<?> connectable, int side, String voltageId) {
        if (connectable instanceof TwoWindingsTransformer twoWindingsTransformer) {
            String voltageLevelId = twoWindingsTransformer.getTerminal(side == 1 ? Branch.Side.ONE : Branch.Side.TWO).getVoltageLevel().getId();
            Optional<Substation> optionalSubstation = twoWindingsTransformer.getSubstation();
            return String.format(
                "%s_%s\t%sAT%s\t",
                voltageLevelId,
                optionalSubstation.isPresent() ? optionalSubstation.get().getId() : "",
                voltageId,
                StringUtils.substringAfter(twoWindingsTransformer.getId(), String.format("TWT %sY", voltageLevelId))
            );
        } else if (connectable instanceof Line line) {
            VoltageLevel voltageLevel = line.getTerminal(side == 1 ? Branch.Side.ONE : Branch.Side.TWO).getVoltageLevel();
            Optional<Substation> optionalSubstation = voltageLevel.getSubstation();
            return String.format(
                "%s_%s\t%sAT%s\t",
                voltageLevel.getId(),
                optionalSubstation.isPresent() ? optionalSubstation.get().getId() : "",
                voltageId,
                StringUtils.substringAfter(line.getId(), String.format("TWT %sY", voltageLevel.getId()))
            );
        }
        return "";
    }
}
