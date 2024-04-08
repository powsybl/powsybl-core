/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

public interface AicArea extends Area {


    /**
     * Get the target AC Net Interchange of this area in MW, using load sign convention
     * @return the AC Net Interchange target
     */
    double getAcNetInterchangeTarget();

    /**
     * Get the net interchange tolerance in MW
     * @return the net interchange tolerance
     */
    double getAcNetInterchangeTolerance();
}
