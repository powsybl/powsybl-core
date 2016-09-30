/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola.contingency;

import eu.itesla_project.contingency.ContingencyElement;
import eu.itesla_project.contingency.ContingencyElementType;
import eu.itesla_project.contingency.tasks.ModificationTask;

import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public abstract class MoContingency implements ContingencyElement {

    private final String id;

    private final String eventDetails;

    private final Map<String, String> eventParameters;

    public MoContingency(String id, String eventDetails, Map<String, String> eventParameters) {
        this.id = id;
        this.eventDetails = eventDetails;
        this.eventParameters = eventParameters;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public Map<String, String> getEventParameters() {
        return eventParameters;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ContingencyElementType getType() {
        return null;
    }

    public abstract MoContingencyElementType getMoType();


    @Override
    public abstract ModificationTask toTask();


    @Override
    public String toString() {
        return eventDetails;
    }
}
