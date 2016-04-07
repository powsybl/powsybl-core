/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.persistence.*;

import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Entity
@Table(name="PARAMETERTABLE")
@DiscriminatorValue("ptable")
public class ParameterTable extends Parameter implements Serializable {
	private static final long serialVersionUID = 1L;

	protected ParameterTable() {
		super();
	}

	public ParameterTable(String name) {
		super();
		this.name=name;
	}

	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
	@JoinColumn(name="TABLE_ID", referencedColumnName="ID")
	@OrderColumn(name="ptindx")
	private List<TableRow> value = new ArrayList<TableRow>();
	
	public List<TableRow> getValue() {
		return value;
	}
	public void setValue(List<TableRow> value) {
		this.value = value;
	}
	
	public void addRow(TableRow row) {
		this.getValue().add(row);
	}
}
