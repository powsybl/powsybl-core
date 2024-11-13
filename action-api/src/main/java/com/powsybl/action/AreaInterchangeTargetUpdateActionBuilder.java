package com.powsybl.action;

public class AreaInterchangeTargetUpdateActionBuilder implements ActionBuilder<AreaInterchangeTargetUpdateActionBuilder> {

    private String id;

    private String areaId;

    private Double target = null;

    @Override
    public String getType() {
        return AreaInterchangeTargetUpdateAction.NAME;
    }

    @Override
    public AreaInterchangeTargetUpdateActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public AreaInterchangeTargetUpdateActionBuilder withNetworkElementId(String elementId) {
        return this;
    }

    public AreaInterchangeTargetUpdateActionBuilder withTarget(Double target) {
        this.target = target;
        return this;
    }

    public AreaInterchangeTargetUpdateActionBuilder withAreaId(String areaId) {
        this.areaId = areaId;
        return this;
    }

    @Override
    public AreaInterchangeTargetUpdateAction build() {
        if (this.target == null) {
            throw new IllegalArgumentException("For a area interchange target update action, a target must be provided");
        }
        return new AreaInterchangeTargetUpdateAction(id, areaId, target);
    }
}
