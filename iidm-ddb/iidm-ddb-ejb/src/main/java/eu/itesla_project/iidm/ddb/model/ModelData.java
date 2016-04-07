/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.Lob;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Embeddable
public class ModelData implements Serializable {
	private static final long serialVersionUID = 1L;

	@Lob
	@Column(name = "BDATA")
	@Basic(fetch = FetchType.LAZY)
	private byte[] data;

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public ModelData() {
	}

	public ModelData(byte[] data) {
		setData(data);
	}

}
