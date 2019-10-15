package com.powsybl.cgmes.conversion.update;

import com.powsybl.iidm.network.Identifiable;

public interface IidmChange {

    public String getVariant();
    
    public void setVariant(String variant);

    public String getAttribute();

    public Identifiable getIdentifiable();

    public String getIdentifiableName();

    public String getIdentifiableId();

    public Object getOldValue();

    public String getOldValueString();

    public Object getNewValue();

    public String getNewValueString();

}
