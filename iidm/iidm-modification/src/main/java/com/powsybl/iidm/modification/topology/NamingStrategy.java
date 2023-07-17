package com.powsybl.iidm.modification.topology;

/**
 * @author Nicolas Rol <nicolas.rol@rte-france.com>
 */
public interface NamingStrategy {

    String getName();

    String getDisconnectorIdPrefix(String id);

    String getDisconnectorIdSuffix(int idNum);

    String getDisconnectorIdSuffix(int id1Num, int id2Num);

    String getDisconnectorId(String id);

    String getDisconnectorId(String id, int idNum);

    String getDisconnectorId(String id, int id1Num, int id2Num);

    String getBreakerIdPrefix(String id);

    String getBreakerIdSuffix(int idNum);

    String getBreakerIdSuffix(int id1Num, int id2Num);

    String getBreakerId(String id);

    String getBreakerId(String id, int idNum);

    String getBreakerId(String id, int id1Num, int id2Num);

    String getSwitchIdPrefix(String id);

    String getSwitchIdSuffix(int idNum);

    String getSwitchIdSuffix(int id1Num, int id2Num);

    String getSwitchId(String id);

    String getSwitchId(String id, int idNum);

    String getSwitchId(String id, int id1Num, int id2Num);

    String getBusbarIdPrefix(String id);

    String getBusbarIdSuffix(int idNum);

    String getBusbarIdSuffix(int id1Num, int id2Num);

    String getBusbarId(String id);

    String getBusbarId(String id, int idNum);

    String getBusbarId(String id, int id1Num, int id2Num);

    String getBusId(String id);

    String getFeederId(String id, String voltageLevelId);
}
