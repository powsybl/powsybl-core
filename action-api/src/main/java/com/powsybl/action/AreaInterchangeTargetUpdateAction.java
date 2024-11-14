package com.powsybl.action;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AreaInterchangeTargetUpdateAction that = (AreaInterchangeTargetUpdateAction) o;
        return target == that.target && Objects.equals(areaId, that.areaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), target, areaId);
    }
}
