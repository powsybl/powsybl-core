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

@Generated(value="Dali", date="2013-01-29T13:04:19.526+0100")
@StaticMetamodel(Equipment.class)
public class Equipment_ {
	public static volatile SingularAttribute<Equipment, Long> id;
	public static volatile SingularAttribute<Equipment, String> cimId;
	public static volatile SingularAttribute<Equipment, ModelTemplateContainer> modelContainer;
	public static volatile SingularAttribute<Equipment, ParametersContainer> parametersContainer;
}
