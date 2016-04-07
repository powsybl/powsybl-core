/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola.service;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.bind.annotation.XmlMimeType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@WebService
@SOAPBinding(style = Style.RPC) // more on this later
public interface SimulatorServer {
    @WebMethod
    @XmlMimeType("application/octet-stream") DataHandler simulate(String inputFileName, String problem, double startTime, double stopTime, int numberOfIntervals, double outputInterval, String method, double tolerance, double fixedstepsize, String resultsFileName, @XmlMimeType("application/octet-stream") DataHandler data);
}