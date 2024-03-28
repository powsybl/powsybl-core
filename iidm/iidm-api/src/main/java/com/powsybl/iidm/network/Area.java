package com.powsybl.iidm.network;

public interface Area <I extends Area<I>> extends Identifiable<I> {

    AreaType getAreaType();

    /**
     * Get the target AC Net Interchange of this area in MW, using load sign convention
     *
     * @return the AC Net Interchange target or NaN if undefined
     */
    double getAcNetInterchangeTarget();

    /**
     * Get the net interchange tolerance (MW, optional)
     *
     * @return the net interchange tolerance or NaN if undefined
     */
    double getAcNetInterchangeTolerance();

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.AREA;
    }
}
