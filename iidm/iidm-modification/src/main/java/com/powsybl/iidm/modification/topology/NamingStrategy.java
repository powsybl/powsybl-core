/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

/**
 * The naming strategy aims at clarifying and facilitating the naming of the different network elements created via the
 * different {@link com.powsybl.iidm.modification.NetworkModification} classes. Based on the name of the network element
 * the user wish to create (a VoltageLeevl, a BranchFeederBay, etc.), all the other elements created during the
 * NetworkModification will be given a name using this name as baseline and prefixes/suffixes according to the naming
 * strategy chosen by the user. The naming strategy can be either the default one
 * ({@link com.powsybl.iidm.modification.topology.DefaultNamingStrategy}) or a new implementation of the present class
 * provided by the user.
 *
 * @author Nicolas Rol <nicolas.rol@rte-france.com>
 */
public interface NamingStrategy {

    String getName();

    String getDisconnectorIdPrefix(String prefixId);

    String getDisconnectorIdSuffix(int idNum);

    String getDisconnectorIdSuffix(int id1Num, int id2Num);

    String getDisconnectorId(String prefixId);

    String getDisconnectorId(String prefixId, int idNum);

    String getDisconnectorId(String prefixId, int id1Num, int id2Num);

    String getBreakerIdPrefix(String prefixId);

    String getBreakerIdSuffix(int idNum);

    String getBreakerIdSuffix(int id1Num, int id2Num);

    String getBreakerId(String prefixId);

    String getBreakerId(String prefixId, int idNum);

    String getBreakerId(String prefixId, int id1Num, int id2Num);

    String getSwitchIdPrefix(String prefixId);

    String getSwitchIdSuffix(int idNum);

    String getSwitchIdSuffix(int id1Num, int id2Num);

    String getSwitchId(String prefixId);

    String getSwitchId(String prefixId, int idNum);

    String getSwitchId(String prefixId, int id1Num, int id2Num);

    String getBusbarIdPrefix(String prefixId);

    String getBusbarIdSuffix(int idNum);

    String getBusbarIdSuffix(int id1Num, int id2Num);

    String getBusbarId(String prefixId);

    String getBusbarId(String prefixId, int idNum);

    String getBusbarId(String prefixId, int id1Num, int id2Num);

    String getBusId(String prefixId);

    String getBusId(String prefixId, String suffixId);

    String getBusId(String prefixId, int idNum);

    String getFeederId(String prefixId, String voltageLevelId);
}
