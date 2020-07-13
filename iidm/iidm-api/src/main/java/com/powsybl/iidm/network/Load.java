/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A constant power load (fixed p0 and q0).
 * <p>p0 and q0 are given at the nominal voltage of the voltage level to which
 * the load is connected (l.getTerminal().getVoltageLevel().getNominalV()).
 *
 * **Characteristics**
 *
 * | Attribute | Type | Unit | Required | Default value | Description |
 * | --------- | ---- | ---- |-------- | ------------- | ----------- |
 * | Id | String | - | yes | - | The ID of the load |
 * | Name | String | - | no | - | The name of the load |
 * | LoadType | `LoadType` | - | no | `UNDEFINED` | The type of the load |
 * | P0 | double | MW | yes | - | The active power setpoint |
 * | Q0 | double | MVar | yes | - | The reactive power setpoint |
 *
 * <p>To create a load, see {@link LoadAdder}
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see LoadAdder
 */
public interface Load extends Injection<Load> {

    LoadType getLoadType();

    Load setLoadType(LoadType loadType);

    /**
     * Get the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getP0();

    /**
     * Set the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    Load setP0(double p0);

    /**
     * Get the constant reactive power in MVar.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getQ0();

    /**
     * Set the constant reactive power in MVar.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    Load setQ0(double q0);

}
