/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2012-12-12T13:24:43.013+0100")
@StaticMetamodel(SimulatorInst.class)
public class SimulatorInst_ {
	public static volatile SingularAttribute<SimulatorInst, Long> id;
	public static volatile SingularAttribute<SimulatorInst, Simulator> simulator;
	public static volatile SingularAttribute<SimulatorInst, String> version;
}
