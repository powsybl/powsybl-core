/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-01-15T17:38:55.742+0100")
@StaticMetamodel(ParametersContainer.class)
public class ParametersContainer_ {
	public static volatile SingularAttribute<ParametersContainer, Long> id;
	public static volatile SingularAttribute<ParametersContainer, String> ddbId;
	public static volatile ListAttribute<ParametersContainer, Parameters> parameters;
}
