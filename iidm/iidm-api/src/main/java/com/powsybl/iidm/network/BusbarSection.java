/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A busbar section, a non impedant element used in a node/breaker substation
 * topology to connect equipments.
 *
 * **Characteristics**
 *
 * | Attribute | Type | Unit | Required | Default value | Description |
 * | --------- | ---- | ---- | -------- | ------------- | ----------- |
 * | Id | String | - | yes | - | The ID of the busbar section |
 * | Name | String | - | no | - | The name of the busbar section |
 * | V | double | kV | no | - | The voltage magnitude of the busbar section |
 * | Angle | double | ° |  no | - | The voltage angle of the busbar section |
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface BusbarSection extends Injection<BusbarSection> {

    double getV();

    double getAngle();
}
