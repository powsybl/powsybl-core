/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-01-29T15:19:18.288+0100")
@StaticMetamodel(ModelTemplate.class)
public class ModelTemplate_ {
	public static volatile SingularAttribute<ModelTemplate, Long> id;
	public static volatile SingularAttribute<ModelTemplate, byte[]> data;
	public static volatile SingularAttribute<ModelTemplate, String> comment;
	public static volatile SingularAttribute<ModelTemplate, SimulatorInst> simulator;
	public static volatile ListAttribute<ModelTemplate, DefaultParameters> defaultParameters;
	public static volatile MapAttribute<ModelTemplate, String, ModelData> modelDataMap;
	public static volatile SingularAttribute<ModelTemplate, String> typeName;
}
