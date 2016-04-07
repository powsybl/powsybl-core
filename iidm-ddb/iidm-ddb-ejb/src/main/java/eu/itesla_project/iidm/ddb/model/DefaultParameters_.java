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

@Generated(value="Dali", date="2013-02-18T12:24:12.978+0100")
@StaticMetamodel(DefaultParameters.class)
public class DefaultParameters_ {
	public static volatile SingularAttribute<DefaultParameters, Long> id;
	public static volatile SingularAttribute<DefaultParameters, Integer> setNum;
	public static volatile ListAttribute<DefaultParameters, Parameter> dpars;
}
