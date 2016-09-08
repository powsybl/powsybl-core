/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server;

import be.pepite.dataserver.api.DataService;
import be.pepite.dataserver.rest.DataserverApplication;
import org.restlet.data.MediaType;

/**
 * Created by pduchesne on 31/03/14.
 */
public class IteslaDataserverApplication
    extends DataserverApplication
{
    public IteslaDataserverApplication(DataService dataService) {
        super(dataService);

        MediaType cond = MediaType.register("text/vnd.itesla.cond", "iTesla rule conditions");
        getMetadataService().addExtension("cond", cond);
    }


}
