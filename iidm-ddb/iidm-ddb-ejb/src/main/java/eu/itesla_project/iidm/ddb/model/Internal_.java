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

@Generated(value="Dali", date="2013-02-22T10:56:11.922+0100")
@StaticMetamodel(Internal.class)
public class Internal_ {
	public static volatile SingularAttribute<Internal, Long> id;
	public static volatile SingularAttribute<Internal, String> nativeId;
	public static volatile SingularAttribute<Internal, SimulatorInst> simulator;
	public static volatile SingularAttribute<Internal, ModelTemplateContainer> modelContainer;
	public static volatile SingularAttribute<Internal, ParametersContainer> parametersContainer;
}
