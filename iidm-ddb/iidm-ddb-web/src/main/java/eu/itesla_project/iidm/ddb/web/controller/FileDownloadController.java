/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.controller;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.enterprise.inject.Model;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import eu.itesla_project.iidm.ddb.model.ModelTemplate;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@Model
public class FileDownloadController {  
  
    private StreamedContent file;  
      
    public FileDownloadController() {          
    	System.out.println("fileDowloadController");
        InputStream stream = ((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/modelTemplateContainer/test.jpg");  
        file = new DefaultStreamedContent(stream, "image/jpg", "test.jpg");  
    }
  
    public StreamedContent getFile() {  
    	System.out.println("return file "+file.getName());
        return file;  
    }
    
    public void setFile(StreamedContent file) {  
        this.file = file;  
    }  
    
    public void downLoadFile(ModelTemplate mt, String mapKey){
    	if (mt!= null) {
    		System.out.println("mt id  "+mt.getId() + " mt comment "+mt.getComment() + "mapkey "+mapKey );
    		byte[]  fileMap=mt.getData(mapKey);
    		;
    		ByteArrayInputStream bis = new ByteArrayInputStream(fileMap);
    		file = new  DefaultStreamedContent(bis, "text/plain", mapKey+".txt");
    		
    		}
    	else {
    		System.out.println("mt is null ");
    		
    	}
    	
    	
    }
    
}  
       