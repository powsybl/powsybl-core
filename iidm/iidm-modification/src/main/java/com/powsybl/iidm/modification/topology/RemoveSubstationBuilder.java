package com.powsybl.iidm.modification.topology;

/**
 * @author Maissa Souissi <maissa.souissi at rte-france.com>
 */
public class RemoveSubstationBuilder {
    private String substationId = null;

    public RemoveSubstation build() {
        return new RemoveSubstation(substationId);
    }

    /**
     * @param substationId the non-null ID of the substation
     */
    public RemoveSubstationBuilder withSubstationId(String substationId) {
        this.substationId = substationId;
        return this;
    }
}
