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

import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.service.DDBManager;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class LazyInternalDataModel extends LazyDataModel<Internal> {

	
private static final long serialVersionUID = 1L;
	
private DDBManager pmanager;
   

   
	
   public LazyInternalDataModel( DDBManager pmanager)
   {
	   this.pmanager=pmanager;
	   setPageSize(10);
	   setRowCount(pmanager.findInternalsAllCount());
   }
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return pmanager.findInternalsAllCount();
	}

	@Override
	public Internal getRowData(String nativeId) {
		return pmanager.findInternal(nativeId);
	}

	@Override
	public Object getRowKey(Internal object) {
		return object.getNativeId();
	}
	
	
	

	@Override
	public List<Internal> load(int first, int pageSize,
			List<SortMeta> multiSortMeta, Map<String, String> filters) {
		return pmanager.findInternalsAllMaxResults(first, pageSize);
	}

	@Override
	public List<Internal> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, String> filters) {
		return pmanager.findInternalsAllMaxResults(first, pageSize);
	}

	
	
}
