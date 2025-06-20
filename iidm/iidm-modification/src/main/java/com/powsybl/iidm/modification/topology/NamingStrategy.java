/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.*;

import java.util.List;

/**
 * The naming strategy aims at clarifying and facilitating the naming of the different network elements created via the
 * different {@link com.powsybl.iidm.modification.NetworkModification} classes. Based on the name of the network element
 * the user wishes to create (a VoltageLevel, a BranchFeederBay, etc.), all the other elements created during the
 * NetworkModification will be given a name using this name as baseline and prefixes/suffixes according to the naming
 * strategy chosen by the user. The naming strategy can be either the default one
 * ({@link com.powsybl.iidm.modification.topology.DefaultNamingStrategy}) or a new implementation of the present class
 * provided by the user.
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public interface NamingStrategy {

    String getName();

    String getSectioningPrefix(String baseId, BusbarSection bbs, int busBarNum, int section1Num, int section2Num);

    String getChunkPrefix(String baseId, List<SwitchKind> switchKindList, int busBarNum, int section1Num, int section2Num);

    String getDisconnectorId(String baseId, int id1Num, int id2Num);

    String getDisconnectorId(BusbarSection bbs, String baseId, int id1Num, int id2Num, int side);

    String getDisconnectorBetweenChunksId(BusbarSection bbs1, String baseId, int id1Num, int id2Num);

    String getBreakerId(String baseId);

    String getBreakerId(String baseId, int id1Num, int id2Num);

    String getSwitchId(String baseId);

    String getSwitchId(String baseId, int idNum);

    String getSwitchId(String baseId, int id1Num, int id2Num);

    String getBusbarId(String baseId, int id1Num, int id2Num);

    String getBusbarId(String baseId, List<SwitchKind> switchKindList, int id1Num, int id2Num);

    String getBusbarId(String baseId, int idNum);

    String getBusId(String baseId);

    /**
     * Used when building a feeder bay
     */
    String getSwitchBaseId(Connectable<?> connectable, int side);

    /**
     * Used in coupling device building
     */
    String getSwitchBaseId(VoltageLevel voltageLevel, BusbarSection bbs1, BusbarSection bbs2);
}
