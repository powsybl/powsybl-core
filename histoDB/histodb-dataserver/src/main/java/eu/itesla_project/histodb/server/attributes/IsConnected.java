/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server.attributes;

import be.pepite.dataserver.api.FunctionalColumn;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 22/10/13
 * Time: 10:36
 * To change this template use File | Settings | File Templates.
 */
public class IsConnected
    extends FunctionalColumn
{
    String attributeName;

    public IsConnected(String attributeName) {

        super(attributeName.substring(0,attributeName.length()-1)+"STATUS", "( dbobj['"+attributeName + "'] !== undefined && !isNaN(dbobj['"+attributeName + "']))");
        this.attributeName = attributeName;
    }

    @Override
    public String[] getRequiredInputs() {
        return new String[] {attributeName};
    }
}
