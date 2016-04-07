/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.util;

import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.ws.security.handler.WSHandlerConstants;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class WsSecurityInterceptor extends WSS4JInInterceptor {

    public WsSecurityInterceptor() {
        super();
       // getProperties().put(WSHandlerConstants.ACTION, "UsernameToken Timestamp");
        getProperties().put(WSHandlerConstants.ACTION, "UsernameToken");
    }
}
