package com.powsybl.iidm.modification.topology;

/**
 * @author Nicolas Rol <nicolas.rol@rte-france.com>
 */

public class DefaultNamingStrategy implements NamingStrategy {

    private static final String SEPARATOR = "_";
    private static final String DISCONNECTOR_NAMEBASE = "DISCONNECTOR";
    private static final String BREAKER_NAMEBASE = "BREAKER";
    private static final String SWITCH_NAMEBASE = "SW";
    private static final String BUS_NAMEBASE = "BUS";

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public String getDisconnectorIdPrefix(String prefixId) {
        return prefixId;
    }

    @Override
    public String getDisconnectorIdSuffix(int idNum) {
        return String.valueOf(idNum);
    }

    @Override
    public String getDisconnectorIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public String getDisconnectorId(String prefixId) {
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + DISCONNECTOR_NAMEBASE;
    }

    @Override
    public String getDisconnectorId(String prefixId, int idNum) {
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + DISCONNECTOR_NAMEBASE + SEPARATOR + getDisconnectorIdSuffix(idNum);
    }

    @Override
    public String getDisconnectorId(String prefixId, int id1Num, int id2Num) {
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + DISCONNECTOR_NAMEBASE + SEPARATOR + getDisconnectorIdSuffix(id1Num, id2Num);
    }

    @Override
    public String getBreakerIdPrefix(String prefixId) {
        return prefixId;
    }

    @Override
    public String getBreakerIdSuffix(int idNum) {
        return String.valueOf(idNum);
    }

    @Override
    public String getBreakerIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public String getBreakerId(String prefixId) {
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + BREAKER_NAMEBASE;
    }

    @Override
    public String getBreakerId(String prefixId, int idNum) {
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + BREAKER_NAMEBASE + SEPARATOR + getBreakerIdSuffix(idNum);
    }

    @Override
    public String getBreakerId(String prefixId, int id1Num, int id2Num) {
        return getBreakerIdPrefix(prefixId) + SEPARATOR + BREAKER_NAMEBASE + SEPARATOR + getBreakerIdSuffix(id1Num, id2Num);
    }

    @Override
    public String getSwitchIdPrefix(String prefixId) {
        return prefixId;
    }

    @Override
    public String getSwitchIdSuffix(int idNum) {
        return String.valueOf(idNum);
    }

    @Override
    public String getSwitchIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public String getSwitchId(String prefixId) {
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + SWITCH_NAMEBASE;
    }

    @Override
    public String getSwitchId(String prefixId, int idNum) {
        return getDisconnectorIdPrefix(prefixId) + SEPARATOR + SWITCH_NAMEBASE + SEPARATOR + getSwitchIdSuffix(idNum);
    }

    @Override
    public String getSwitchId(String prefixId, int id1Num, int id2Num) {
        return getSwitchIdPrefix(prefixId) + SEPARATOR + SWITCH_NAMEBASE + SEPARATOR + getSwitchIdSuffix(id1Num, id2Num);
    }

    @Override
    public String getBusbarIdPrefix(String prefixId) {
        return prefixId;
    }

    @Override
    public String getBusbarIdSuffix(int idNum) {
        return String.valueOf(idNum);
    }

    @Override
    public String getBusbarIdSuffix(int id1Num, int id2Num) {
        return id1Num + SEPARATOR + id2Num;
    }

    @Override
    public String getBusbarId(String prefixId) {
        return getBusbarIdPrefix(prefixId);
    }

    @Override
    public String getBusbarId(String prefixId, int idNum) {
        return getBusbarIdPrefix(prefixId) + SEPARATOR + getBusbarIdSuffix(idNum);
    }

    @Override
    public String getBusbarId(String prefixId, int id1Num, int id2Num) {
        return getBusbarIdPrefix(prefixId) + SEPARATOR + getBusbarIdSuffix(id1Num, id2Num);
    }

    @Override
    public String getBusId(String prefixId) {
        return prefixId + SEPARATOR + BUS_NAMEBASE;
    }

    @Override
    public String getFeederId(String id, String voltageLevelId) {
        return id + SEPARATOR + voltageLevelId;
    }
}
