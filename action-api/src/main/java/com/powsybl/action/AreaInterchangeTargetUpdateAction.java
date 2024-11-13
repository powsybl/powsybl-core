package com.powsybl.action;

public class AreaInterchangeTargetUpdateAction extends AbstractAction {

    public static final String NAME = "AREA_INTERCHANGE_TARGET_UPDATE_ACTION";

    private final String areaId;
    private final double target;

    public AreaInterchangeTargetUpdateAction(String id, String areaId, double target) {
        super(id);
        this.areaId = areaId;
        this.target = target;
    }

    public double getTarget() {
        return target;
    }

    public String getAreaId() {
        return areaId;
    }

    @Override
    public String getType() {
        return NAME;
    }
}
