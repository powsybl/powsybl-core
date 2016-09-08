/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server.attributes;

import be.pepite.dataserver.api.ColumnDescriptor;
import be.pepite.dataserver.api.FunctionalColumn;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 22/10/13
 * Time: 10:36
 * To change this template use File | Settings | File Templates.
 */
public class CurrentPowerRatio
    extends FunctionalColumn
{
    String terminalId;

    public CurrentPowerRatio(String terminalId) {
        //super(terminalId+"_IP", "( typeof "+terminalId + "_I !== 'undefined' && typeof "+terminalId + "_P !== 'undefined' && ("+terminalId + "_I / "+terminalId + "_P) ) || null");
        super(terminalId+"_IP", "( dbobj['"+terminalId + "_I'] !== undefined && dbobj['"+terminalId + "_P'] !== undefined && (dbobj['"+terminalId + "_I'] / dbobj['"+terminalId + "_P']) ) || null");

        this.terminalId = terminalId;
    }

    @Override
    public String[] getRequiredInputs() {
        return new String[] {terminalId+"_I", terminalId+"_P"};
    }
}
