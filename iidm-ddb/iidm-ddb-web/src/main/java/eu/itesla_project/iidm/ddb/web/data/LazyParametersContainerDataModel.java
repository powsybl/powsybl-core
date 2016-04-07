/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.data;

import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;


import eu.itesla_project.iidm.ddb.model.ParametersContainer;
import eu.itesla_project.iidm.ddb.service.DDBManager;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class LazyParametersContainerDataModel extends LazyDataModel<ParametersContainer> {

	
	private static final long serialVersionUID = 1L;
		
	private DDBManager pmanager;
   

   
	
   public LazyParametersContainerDataModel( DDBManager pmanager)
   {
	   this.pmanager=pmanager;
	   setPageSize(10);
	   setRowCount(pmanager.findParametersContainerAllCount());
   }
	
	@Override
	public int getRowCount() {
		return pmanager.findParametersContainerAllCount();
	}

	@Override
	public ParametersContainer getRowData(String id) {
		return pmanager.findParametersContainer(id);
	}

	@Override
	public Object getRowKey(ParametersContainer object) {
		return object.getDdbId();
	}
	
	
	

	@Override
	public List<ParametersContainer> load(int first, int pageSize,
			List<SortMeta> multiSortMeta, Map<String, String> filters) {
		return pmanager.findParametersContainerAllMaxResults(first, pageSize);
	}

	@Override
	public List<ParametersContainer> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, String> filters) {
		List<ParametersContainer> res = pmanager.findParametersContainerAllMaxResults(first, pageSize);
		return res;
	}

	
	
}
