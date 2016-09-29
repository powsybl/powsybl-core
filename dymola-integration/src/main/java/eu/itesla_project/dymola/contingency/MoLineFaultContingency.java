/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola.contingency;

import eu.itesla_project.contingency.tasks.ModificationTask;

import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class MoLineFaultContingency extends MoContingency {

    public MoLineFaultContingency(String id, String eventDetails, Map<String, String> eventParameters) {
        super(id, eventDetails, eventParameters);
    }

    @Override
    public MoContingencyElementType getMoType() {
        return MoContingencyElementType.MO_LINE_FAULT;
    }

    //TODO  (is a BranchTripping, used in LineFaultContingency, ok for this fault ?)
    @Override
    public ModificationTask toTask() {
        return null;
    }

    public double getT1() {return Double.parseDouble(getEventParameters().get("time_1"));}
    public double getT2() {return Double.parseDouble(getEventParameters().get("time_2"));}

}
