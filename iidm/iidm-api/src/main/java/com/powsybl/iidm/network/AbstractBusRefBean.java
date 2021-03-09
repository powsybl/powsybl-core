/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@JsonSubTypes({
        @JsonSubTypes.Type(value = BranchBasedBusBean.class),
        @JsonSubTypes.Type(value = IdBasedBusBean.class),
        @JsonSubTypes.Type(value = InjectionBasedBusBean.class),
        @JsonSubTypes.Type(value = NodeNumberBasedBusBean.class)
})
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
public abstract class AbstractBusRefBean implements Serializable {
}
