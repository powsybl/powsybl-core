package com.powsybl.ucte.converter.export.elements;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Load;
import com.powsybl.ucte.network.UcteNode;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class LoadUcteExport {

    private LoadUcteExport() {
    }

    /**
     * Initialize the power consumption fields from the loads connected to the specified bus.
     *
     * @param ucteNode The UCTE node to fill
     * @param bus The bus the loads are connected to
     */
    public static void convertLoads(UcteNode ucteNode, Bus bus) {
        ucteNode.setActiveLoad(bus.getLoadStream().mapToDouble(Load::getP0).sum());
        ucteNode.setReactiveLoad(bus.getLoadStream().mapToDouble(Load::getQ0).sum());
    }
}
