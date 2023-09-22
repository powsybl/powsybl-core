/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * RTE naming strategy
 * TODO: move this class to powsybl-rte
 * @author Nicolas Rol <nicolas.rol@rte-france.com>
 */
public class RteNamingStrategy implements NamingStrategy {

    private static final String SEPARATOR = ".";
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
    public String getDisconnectorId(BusbarSection bbs, String prefixId, int id1Num, int id2Num) {
        // In this case, multiple busbar are parallel
        BusbarSectionPosition position = bbs.getExtension(BusbarSectionPosition.class);
        if (position == null) {
            throw new PowsyblException("Multiple busbar are needed");
        }
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + DISCONNECTOR_NAMEBASE + SEPARATOR + getDisconnectorIdSuffix(id1Num, position.getBusbarIndex());
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
    public final String getSwitchBaseId(Connectable<?> connectable, int side) {
        if (connectable instanceof TwoWindingsTransformer twoWindingsTransformer) {
            return getSwitchBaseIdOnTwoWindingsTransformer(twoWindingsTransformer, side);
        } else if (connectable instanceof Line line) {
            return getSwitchBaseIdOnLine(line, side);
        } else {
            // TODO: implement the other cases
            throw new PowsyblException("To be implemented");
        }
    }

    public final String getSwitchBaseIdOnTwoWindingsTransformer(TwoWindingsTransformer twoWindingsTransformer, int side) {
        if (side == 0) {
            throw new PowsyblException("Side should not be equal to 0 for a TwoWindingsTransformer");
        }
        String voltageLevelId = twoWindingsTransformer.getTerminal(side == 1 ? Branch.Side.ONE : Branch.Side.TWO).getVoltageLevel().getId();
        Optional<Substation> optionalSubstation = twoWindingsTransformer.getSubstation();
        String voltageId = voltageLevelId.substring(voltageLevelId.length() - 1);
        return String.format(
            "%s_%s\t%sAT%s\t",
            voltageLevelId,
            optionalSubstation.isPresent() ? optionalSubstation.get().getId() : "",
            voltageId,
            StringUtils.substringAfter(twoWindingsTransformer.getId(), String.format("TWT %sY", voltageLevelId))
        );
    }

    public final String getSwitchBaseIdOnLine(Line line, int side) {
        if (side == 0) {
            throw new PowsyblException("Side should not be equal to 0 for a Line");
        }
        // Ids on local side
        VoltageLevel voltageLevel = line.getTerminal(side == 1 ? Branch.Side.ONE : Branch.Side.TWO).getVoltageLevel();
        Optional<Substation> optionalSubstation = voltageLevel.getSubstation();

        // Ids on opposite side
        VoltageLevel voltageLevelOpposite = line.getTerminal(side == 2 ? Branch.Side.ONE : Branch.Side.TWO).getVoltageLevel();
        Optional<Substation> optionalSubstationOpposite = voltageLevelOpposite.getSubstation();

        // Line information
        String lineIntel = StringUtils.startsWith(line.getId(), voltageLevel.getId()) ?
            StringUtils.substringBefore(StringUtils.substringAfter(line.getId(), voltageLevel.getId()), voltageLevelOpposite.getId()) :
            StringUtils.substringBefore(StringUtils.substringAfter(line.getId(), voltageLevelOpposite.getId()), voltageLevel.getId());

        return String.format(
            "%s_%s\t%s%s.%s\t",
            voltageLevel.getId(),
            optionalSubstation.isPresent() ? optionalSubstation.get().getId() : "",
            voltageLevel.getId().substring(voltageLevel.getId().length() - 1),
            optionalSubstationOpposite.isPresent() ? optionalSubstationOpposite.get().getId() : "",
            lineIntel.substring(lineIntel.length() - 1)
        );
    }

    @Override
    public final String getVoltageLevelId(Substation substation, String prefix, String suffix) {
        return substation.getId() + "P" + suffix;
    }

    @Override
    public String getBbsId(int number) {
        return String.valueOf(number);
    }

    @Override
    public String getBbsId(int number, String letter) {
        return number + letter;
    }

    @Override
    public String getBbsId(int firstNumber, int secondNumber) {
        return firstNumber + SEPARATOR + secondNumber;
    }

    @Override
    public String getBbsId(int firstNumber, String letter, int secondNumber) {
        return firstNumber + letter + secondNumber;
    }

    @Override
    public final String getGeneratorId(Substation substation, EnergySource energySource, String prefix, String suffix, int code) {
        String energyCode = switch (energySource) {
            case NUCLEAR -> "N";
            case THERMAL -> "T";
            case HYDRO -> "H";
            case WIND -> "E";
            case SOLAR -> "V";
            case OTHER -> "X";
        };
        return substation.getId() + energyCode + "G" + code;
    }
}
