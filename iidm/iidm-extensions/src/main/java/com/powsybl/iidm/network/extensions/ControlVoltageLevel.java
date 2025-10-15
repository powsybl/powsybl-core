package com.powsybl.iidm.network.extensions;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface ControlVoltageLevel {

    String getId();

    boolean forceOneTransformerLoads();
}
