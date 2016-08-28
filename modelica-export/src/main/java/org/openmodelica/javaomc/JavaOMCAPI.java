/*
 * Copyright (c) 2010, Institute of High Performance Computing
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 * http://www.opensource.org/licenses/bsd-license.php)
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Institute of High Performance Computing
 *   nor the names of its contributors may be used to endorse or 
 *   promote products derived from this software without specific 
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.openmodelica.javaomc;

import java.util.List;

public class JavaOMCAPI extends OMCProxy {
	/**
	 * Constructor
	 * 
	 * This constructor initialize CORBA connection with OMC and set 
	 * the OPENMODELICALIBRARY environtment variable. 
	 */
	public JavaOMCAPI() {
		// TODO Auto-generated constructor stub
		String omLibPath=System.getenv("OPENMODELICALIBRARY");
		try
		{
			init();
			setModelicaLibraryPath(omLibPath);
		}
		catch(Exception ex)
		{
			System.out.println(
					  "\nError when initializing OMC connection. \n"+
					  ex.getMessage());
		}
	}
	/**
	 * get class names in the global scope, similar to getClassName() in OMC-API.
	 * 
	 * @return	A string array containing class names definitions in the global scope.
	 * 			if error return a string "$Error$".
	 * @throws ConnectException
	 */
	public String[] getClassNamesList() throws ConnectException
	{
		String retvaltemp=sendExpression("getClassNames()");
		String[] retval={"$Error$"};
		if(retvaltemp.contains("Error")){
			return retval;
		}
		else{
			retval=retvaltemp.substring(1, retvaltemp.length()-2).split(",");
			return retval;
		}
	}
	
	/**
	 * get class names inside of a class, similar to getClassName(class) in OMC-API.
	 * 
	 * @param	className	is the parent class that contains the class names we look for.
	 * @return	A string array containing class names definitions in the global scope.
	 * 			if error return a string "$Error$".
	 * @throws ConnectException
	 */
	public String[] getClassNamesList(String className) throws ConnectException
	{
		String retvaltemp=super.getClassNames(className);
		String[] retval={"$Error$"};
		if(retvaltemp.contains("Error")){
			return retval;
		}
		else{
			retval=retvaltemp.substring(1,retvaltemp.length()-2).split(",");
			return retval;
		}
	}
	/**
	 * get the type of the class restriction.
	 *  
	 * @param classname 	is the class that we want to know its restriction type.
	 * @return				a string specifying the class restriction, e.g. "model", 
	 * 						"connector", etc.
	 * @throws ConnectException
	 */
	public String getClassRestriction(String classname) throws ConnectException
	{
		String retval=sendExpression("getClassRestriction("+classname+")");
		return retval;
	}

	/**
	 * check whether the class has a type Model.
	 * 
	 * @param classname		the class that we want to check its type.
	 * @return				true if it is a model, and false otherwise.
	 * @throws ConnectException
	 */
	public boolean isModel(String classname) throws ConnectException
	{
		String retval=sendExpression("isModel("+classname+")");
		return retval.contains("true");
	}

	/**
	 * check whether the class is a Connector type.
	 * 
	 * @param classname		the name of the class which we want to check its type.
	 * @return				true if it is  a connector, and false if otherwise.
	 * @throws ConnectException
	 */
	public boolean isConnector(String classname) throws ConnectException
	{
		String retval=sendExpression("isConnector("+classname+")");
		return retval.contains("true");
	}

	/**
	 * check whether the class is a Record type.
	 * 
	 * @param classname		the class name which we want to check its type.
	 * @return				true if is a Record, false if otherwise. 
	 * @throws ConnectException
	 */
	public boolean isRecord(String classname) throws ConnectException
	{
		String retval=sendExpression("isRecord("+classname+")");
		return retval.contains("true");
	}

	/**
	 * check whether the class is a Block type.
	 * 
	 * @param classname		the class name which we want to check its type.
	 * @return				true if it is a Block, false if otherwise.
	 * @throws ConnectException
	 */
	public boolean isBlock(String classname) throws ConnectException
	{
		String retval=sendExpression("isBlock("+classname+")");
		return retval.contains("true");
	}

	/**
	 * check whether the class is a Type.
	 *  
	 * @param classname		the class name which we want to check its type.
	 * @return				true if it is a Type, false if otherwise.
	 * @throws ConnectException
	 */
	public boolean isType(String classname) throws ConnectException
	{
		String retval=sendExpression("isType("+classname+")");
		return retval.contains("true");
	}

	/**
	 * check whether the class is a Function type.
	 * 
	 * @param classname		the class name which we want to check its type.
	 * @return				true if it is a Function, false if otherwise.
	 * @throws ConnectException
	 */
	public boolean isFunction(String classname) throws ConnectException
	{
		String retval=sendExpression("isFunction("+classname+")");
		return retval.contains("true");
	}

	/**
	 * check whether the class is a Parameter.
	 * 
	 * @param classname		the class name which we want to check.
	 * @return				true if it is a Parameter, false if otherwise.
	 * @throws ConnectException
	 */
	public boolean isParameter(String classname) throws ConnectException
	{
		String retval=sendExpression("isParameter("+classname+")");
		return retval.contains("true");
	}

	/**
	 * check whether the class is a Constant.
	 * 
	 * @param classname		the class name which we want to check its type.
	 * @return				true if it is a Constant, false if otherwise.
	 * @throws ConnectException
	 */
	public boolean isConstant(String classname) throws ConnectException
	{
		String retval=sendExpression("isConstant("+classname+")");
		return retval.contains("true");
	}
	
	/**
	 * check whether the class has Protected type.
	 * 
	 * @param classname		the class name which we want to check.
	 * @return				true if it is Protected, false if otherwise.
	 * @throws ConnectException
	 */
	public boolean isProtected(String classname) throws ConnectException
	{
		String retval=sendExpression("isProtected("+classname+")");
		return retval.contains("true");
	}

	/**
	 * get element info.
	 * 
	 * @param classname		the class name which elements info we want to obtain.
	 * @return				string containing all the elements of the class.
	 * @throws ConnectException
	 */
	public String getElementsInfo(String classname) throws ConnectException
	{
		String retval=sendExpression("getElementsInfo("+classname+")");
		return retval;
	}
	
	/**
	 * get a list of information about a class.
	 * 
	 * @param classname		the class name which information we want to get.
	 * @return				a String containing information of the class separated by comma.
	 * @throws ConnectException
	 */
	public String getClassInformation(String classname) throws ConnectException
	{
		String retval=sendExpression("getClassInformation("+classname+")");
		return retval;
	}
	
	/**
	 * get icon annotation of the class.
	 * 
	 * @param classname		the class name which icon annotation we want to get.
	 * @return				a String containing the icon annotation.
	 * @throws ConnectException
	 */
	public String getIconAnnotation(String classname) throws ConnectException
	{
		String retval=sendExpression("getIconAnnotation("+classname+")");
		return retval;
	}
	
	/**
	 * get diagram annotation of the class.
	 * 
	 * @param classname		the class name which diagram annotation we want to get.
	 * @return				a String containing diagram annotation.
	 * @throws ConnectException
	 */
	public String getDiagramAnnotation(String classname) throws ConnectException
	{
		String retval=sendExpression("getDiagramAnnotation("+classname+")");
		return retval;
	}
	
	/**
	 * get documentation annotation of a class.
	 * 
	 * @param classname		the class name which documentation annotation we want to get.
	 * @return				a String which contains documentation annotation of the class.
	 * @throws ConnectException
	 */
	public String getDocumentationAnnotation(String classname) throws ConnectException
	{
		String retval=sendExpression("getDocumentationAnnotation("+classname+")");
		return retval;
	}
	
	/**
	 * loads all models in the file fname.
	 * @param fname		file name containing models.
	 * @return			true if successful, false otherwise.
	 * @throws ConnectException
	 */
	public boolean loadFile(String fname) throws ConnectException
	{
		String retval=sendExpression("loadFile(\""+fname+"\")");
		return retval.contains("true");
	}
	
	/**
	 * load the model by looking the correct file at $OPENMODELICALIBRARY
	 * 
	 * @param modelname		the model name to load.	
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean loadModel(String modelname) throws ConnectException
	{
		String retval=sendExpression("loadModel("+modelname+")");
		return retval.contains("true");
	}
	
	/**
	 * create new model.
	 * 
	 * @param modelname		is the model name which we want to create.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean createModel(String modelname) throws ConnectException
	{
		String retval=sendExpression("createModel("+modelname+")");
		return retval.contains("true");
	}
	
	/**
	 * create a new empty model name modelname inside classname.
	 * 
	 * @param modelname		the name of the new model.
	 * @param classname		the class name where the new model will be.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean newModel(String modelname, String classname) throws ConnectException
	{
		String retval=sendExpression("newModel("+modelname+","+classname+")");
		return retval.contains("Ok");
	}
	
	/**
	 * saves the model into the file it was previously linked to.
	 * 
	 * @param modelname		the name of the model to be saved.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean save(String modelname) throws ConnectException
	{
		String retval=sendExpression("save("+modelname+")");
		return retval.contains("true");
	}
	
	/**
	 * delete the class from the symbol table.
	 * 
	 * @param classname		the name of the class to be deleted.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean deleteClass(String classname) throws ConnectException
	{
		String retval=sendExpression("deleteClass("+classname+")");
		return retval.contains("true");
	}
	
	/**
	 * Renames an already existing class to a new one.
	 * 
	 * @param classnameold		name of the class to be renamed.
	 * @param classnamenew		the new name of the class.
	 * @return					true if successful.
	 * @throws ConnectException
	 */
	public boolean renameClass(String classnameold, String classnamenew) throws ConnectException
	{
		String retval=sendExpression("renameClass("+classnameold+","+classnamenew+")");
		return retval.contains("true");
	}
	
	/**
	 * set the class string comment.
	 * 
	 * @param classname		the class name for the comment to be set.
	 * @param comment		the comment for the class.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean setClassComment(String classname, String comment) throws ConnectException
	{
		String retval=sendExpression("setClassComment("+classname+",\""+comment+"\")");
		return retval.contains("Ok");
	}
	
	/**
	 * adds annotation to the class.
	 * 
	 * @param classname		class name which annotation is to be set.
	 * @param annot			annotation for the class.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean addClassAnnotation(String classname, String annot) throws ConnectException
	{
		String retval=sendExpression("addClassAnnotation("+classname+",annotate="+annot+")");
		return retval.contains("true");
	}
	
	/**
	 * returns the names of all package definitions in the global scope.
	 * 
	 * @return		a list of package names.
	 * @throws ConnectException
	 */
	public String[] getPackages() throws ConnectException
	{
		String retvaltemp=sendExpression("getPackages()");
		String[] retval=retvaltemp.substring(1,retvaltemp.length()-2).split(",");
		return retval;
	}
	
	/**
	 * returns the names of all packages in the class/package parentname.
	 * 
	 * @param parentname		the parent class which we want to get the packages names.
	 * @return					a list of all the package names.
	 * @throws ConnectException
	 */
	public String[] getPackages(String parentname) throws ConnectException
	{
		String retvaltemp=sendExpression("getPackages("+parentname+")");
		String[] retval=retvaltemp.substring(1,retvaltemp.length()-2).split(",");
		return retval;
	}
	
	/**
	 * returns all the possible information of a class.
	 * 
	 * @param classname		the name of the class we seek information of.
	 * @return				a string containing attribute=value list.
	 * @throws ConnectException
	 */
	public String getClassAttributes(String classname) throws ConnectException
	{
		String retval=sendExpression("getClassAttributes("+classname+")");
		return retval;
	}
	
	/**
	 * check whether the class exists or not.
	 * 
	 * @param classname		class name to be checked.
	 * @return				true if class exists.
	 * @throws ConnectException
	 */
	public boolean existClass(String classname) throws ConnectException
	{
		String retval=sendExpression("existClass("+classname+")");
		return retval.contains("true");
	}
	
	/**
	 * check whether the package exists or not.
	 * 
	 * @param classname		package name to check.
	 * @return				true if packcage exists.
	 * @throws ConnectException
	 */
	public boolean existPackage(String classname) throws ConnectException
	{
		String retval=sendExpression("existPackage("+classname+")");
		return retval.contains("true");
	}
	
	/**
	 * check whether model exists or not.
	 * 
	 * @param classname		model name to check.
	 * @return				true if model exists.
	 * @throws ConnectException
	 */
	public boolean existModel(String classname) throws ConnectException
	{
		String retval=sendExpression("existModel("+classname+")");
		return retval.contains("true");
	}
	
	/**
	 * return a list of component declarations within a class.
	 * 
	 * @param classname		the class of the components we wish to obtain.
	 * @return				a string containing component declarations.
	 * @throws ConnectException
	 */
	public String getComponents(String classname) throws ConnectException
	{
		String retval=sendExpression("getComponents("+classname+")");
		return retval;
	}
	
	/**
	 * set properties of compname inside classname.
	 * 
	 * @param classname		class name containing compname.
	 * @param compname		compname which properties we wish to set.
	 * @param finalflag		final={true,false}
	 * @param flowflag		flow={true,false}
	 * @param protectedflag	protected={true,false}
	 * @param replaceflag	replacable={true,false}
	 * @param varflag		variability={"constant","discrete","parameter", ""}
	 * @param a8			Undefined in OMC-API, set to "{true,false}" in example.
	 * @param a9			Undefined in OMC-API, set to "input" in example.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean setComponentProperties(String classname, String compname,
			boolean finalflag, boolean flowflag, boolean protectedflag,
			boolean replaceflag, String varflag, String a8, String a9) throws ConnectException
	{
		String retval=sendExpression("setComponentProperties("+classname+
				","+compname+",{"+finalflag+","+flowflag+","+protectedflag+
				","+replaceflag+"},{\""+varflag+"\"},"+a8+",{\""+a9+"\"})");
		return retval.contains("Ok");
	}
	
	/**
	 * set comment on a component inside a class.
	 * 
	 * @param classname		class name containing component.
	 * @param compname		component name.
	 * @param comment		comment.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean setComponentComment(String classname, String compname, 
			String comment) throws ConnectException
	{
		String retval=sendExpression("setComponentComment("+classname+","+
				compname+",\""+comment+"\")");
		return retval.contains("Ok");
	}
	
	/**
	 * return a list of all annotations of all components in a class.
	 * 
	 * @param classname		the class containing components' annotation we wish to obtain.
	 * @return				a String containing component annotations.
	 * @throws ConnectException
	 */
	public String getComponentAnnotations(String classname) throws ConnectException
	{
		String retval=sendExpression("getComponentAnnotations("+classname+")");
		return retval;
	}
	
	/**
	 * add a component with a name compname, with a type comptype, inside classname.
	 * 
	 * @param compname		component name.
	 * @param comptype		component type.
	 * @param classname		class which is to contain compname.
	 * @param annot			annotation for the component.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean addComponent(String compname, String comptype, String classname, 
			String annot) throws ConnectException
	{
		String retval="";
		if(annot!="")
		{
			retval=sendExpression("addComponent("+compname+","+comptype+","+classname+
					",annotate="+annot+")");
		}
		else
		{
			retval=sendExpression("addComponent("+compname+","+comptype+","+classname+")");
		}
		return retval.contains("true");
	}
	
	/**
	 * delete a component inside a class.
	 * 
	 * @param compname		component name.
	 * @param classname		class containing compname.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean deleteComponent(String compname, String classname) throws ConnectException
	{
		String retval=sendExpression("deleteComponent("+compname+","+classname+")");
		return retval.contains("true");
	}
	
	/**
	 * update an already existing component with the name compname, type comptype, inside 
	 * classname. optional annotations are given.
	 * 
	 * @param compname		component name.
	 * @param comptype		component type.
	 * @param classname		class containing compname.
	 * @param annot			annotation.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean updateComponent(String compname, String comptype, 
			String classname, String annot) throws ConnectException
	{
		String retval="";
		if(annot!="")
		{
			retval=sendExpression("updateComponent("+compname+","+comptype+","+classname+
					",annotate="+annot+")");
		}
		else
		{
			retval=sendExpression("updateComponent("+compname+","+comptype+","+classname+
					",annotate=\" \")");
		}
		return retval.contains("true");
	}
	
	/**
	 * rename an already existing component with the name compname, inside classname, 
	 * to a newname.
	 * 
	 * @param classname		class name containing compname.
	 * @param compname		component name to be renamed.
	 * @param newname		new name.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean renameComponent(String classname, String compname, 
			String newname) throws ConnectException
	{
		String retval=sendExpression("renameComponent("+classname+","+compname+","+newname+")");
		return retval.contains(newname);
	}
	
	/**
	 * return the flattened annotation record of the nth component inside a class.
	 * 
	 * @param classname 	class name containing component.
	 * @param index			index of the component starting from 1.
	 * @return				String containing component annotation.
	 * @throws ConnectException
	 */
	public String getNthComponentAnnotation(String classname, String index) throws ConnectException
	{
		String retval=sendExpression("getNthComponentAnnotation("+classname+","+index+")");
		return retval;
	}
	
	/**
	 * return the modification of the nth component inside a class.
	 * 
	 * @param classname		classname containing the component.
	 * @param index			index of the component starting from 1.
	 * @return				a String containing the modification.
	 * @throws ConnectException
	 */
	public String getNthComponentModification(String classname, String index) throws ConnectException
	{
		String retval=sendExpression("getNthComponentModification("+classname+","+index+")");
		return retval.substring(1, retval.length()-2);
	}
	
	/**
	 * return the value of a component.
	 * 
	 * @param classname		class name containing the component.
	 * @param compname		component name.
	 * @return				a STring containing component value.
	 * @throws ConnectException
	 */
	public String getComponentModifierValue(String classname, 
			String compname) throws ConnectException
	{
		String retval=sendExpression("getComponentModifierValue("+classname+","+
					compname+")");
		return retval;
	}
	
	/**
	 * set the modifier value of a component inside a class.
	 * 
	 * @param classname		class name containing the component.
	 * @param compname		component name.
	 * @param value			modifier value.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean setComponentModifierValue(String classname, 
			String compname, String value) throws ConnectException
	{
		String retval=sendExpression("setComponentModifierValue("+classname+","+
					compname+","+value+")");
		return retval.contains("Ok");
	}
	
	/**
	 * return a list of the names of all component modifier in the class.
	 * 
	 * @param classname		class name containing the component.
	 * @param compname		component name.
	 * @return				a list of component modifier names.
	 * @throws ConnectException
	 */
	public String[] getComponentModifierNames(String classname, 
			String compname) throws ConnectException
	{
		String retvaltemp=sendExpression("getComponentModifierNames("+classname+","+compname+")");
		String[] retval=retvaltemp.substring(1, retvaltemp.length()-2).split(",");
		return retval;
	}
	
	/**
	 * returns the number of inherited classes of a class.
	 * 
	 * @param classname		class name to check its inheritance.
	 * @return				integer, number of classes inheritance.
	 * @throws ConnectException
	 */
	public int getInheritanceCount(String classname) throws ConnectException
	{
		String retval=sendExpression("getInheritanceCount("+classname+")");
		return Integer.parseInt(retval.trim());
	}
	
	/**
	 * returns the name of the nth inherited class of classname.
	 * 
	 * @param classname		class name to check its inheritance.
	 * @param index			index of inherited class to obtain, starting from 1.
	 * @return				class name of inherited class.
	 * @throws ConnectException
	 */
	public String getNthInheritedClass(String classname, String index) throws ConnectException
	{
		String retval=sendExpression("getNthInheritedClass("+classname+","+index+")");
		return retval;
	}
	
	/**
	 * returns the number of connections in the model.
	 * 
	 * @param classname		name of the model.
	 * @return				integer, number of connections.
	 * @throws ConnectException
	 */
	public int getConnectionCount(String classname) throws ConnectException
	{
		String retval=sendExpression("getConnectionCount("+classname+")");
		return Integer.parseInt(retval.trim());
	}
	
	/**
	 * set comment at a connection equation.
	 * 
	 * @param classname		model name containing the connection.
	 * @param comp1name		connection one.
	 * @param comp2name		connection two.
	 * @param comment		comment.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean setConnectionComment(String classname, 
			String comp1name, String comp2name,
			String comment) throws ConnectException
	{
		String retval=sendExpression("setConnectionComment("+classname+","+comp1name+
					","+comp2name+",\""+comment+"\")");
		return retval.contains("Ok");
	}
	
	/**
	 * returns the nth connection declared in classname.
	 * 
	 * @param classname		class name containing the connection.
	 * @param index			index of the connection, starting from 1.
	 * @return				returns a list containing connection one,
	 * 						connection two, and its comment.
	 * @throws ConnectException
	 */
	public String[] getNthConnection(String classname, String index) throws ConnectException
	{
		String retvaltemp=sendExpression("getNthConnection("+classname+","+index+")");
		return retvaltemp.substring(1, retvaltemp.length()-2).split(",");
	}

	/**
	 * returns the annotation of the nth connection inside classname.
	 * 
	 * @param classname		class name containing the connection. 
	 * @param index			index of the connection, starting from 1.
	 * @return				String containing the annotation of the connection.
	 * @throws ConnectException
	 */
	public String getNthConnectionAnnotation(String classname, String index) throws ConnectException
	{
		String retval=sendExpression("getNthConnectionAnnotation("+classname+","+index+")");
		return retval;
	}
	
	/**
	 * adds connection connect(c1,c2) with annotation.
	 * 
	 * @param c1		connection one.
	 * @param c2		connection two.
	 * @param classname	class name containing the connection.	
	 * @param annot		annotation for the connection.
	 * @return			true if successful.
	 * @throws ConnectException
	 */
	public boolean addConnection(String c1, String c2, String classname, 
			String annot) throws ConnectException
	{
		String retval="";
		if(annot!="")
		{
			retval=sendExpression("addConnection("+c1+","+c2+","+classname+
					",annotate="+annot+")");
		}
		else
		{
			retval=sendExpression("addConnection("+c1+","+c2+","+classname+")");				
		}
		return retval.contains("Ok");
	}
	
	/**
	 * deletes the connection connect(c1,c2) inside a class.
	 * 
	 * @param c1		connection one.
	 * @param c2		connection two.
	 * @param classname	class name containing the connection.
	 * @return			true if successful.
	 * @throws ConnectException
	 */
	public boolean deleteConnection(String c1, String c2, String classname) throws ConnectException
	{
		String retval=sendExpression("deleteConnection("+c1+","+c2+","+classname+")");
		return retval.contains("Ok");
	}

	/**
	 * updates an already existing connection.
	 * 
	 * @param con1		connection one.
	 * @param con2		connection two.
	 * @param classname	class name containing the connection.
	 * @param annot		annotation for the connection.
	 * @return			true if successful.
	 * @throws ConnectException
	 */
	public boolean updateConnection(String con1, String con2, 
			String classname, String annot) throws ConnectException
	{
		String retval = sendExpression("updateConnection("+con1+","+con2+","+
					classname+",annotate="+annot+")");
		return retval.contains("Ok");
	}
	
	/**
	 * get current working directory.
	 * 
	 * @return	the current working directory.
	 * @throws ConnectException
	 */
	public String cd() throws ConnectException
	{
		String retval=sendExpression("cd()");
		return retval;
	}

	/**
	 * change to the specified directory.
	 * 
	 * @param newdir	new directory.
	 * @return			true if successful.
	 * @throws ConnectException
	 */
	public boolean cd(String newdir) throws ConnectException
	{
		String retval=sendExpression("cd(\""+newdir+"\")");
		return retval.contains(newdir);
	}
	
	/**
	 * Instantiates model and check whether it has any error.
	 * 
	 * @param modelname		model name to check.
	 * @return				true if successful in instantiating the model.
	 * @throws ConnectException
	 */
	public boolean checkModel(String modelname) throws ConnectException
	{
		String retval=sendExpression("checkModel("+modelname+")");	
		return retval.contains("successful");
	}
	
	/**
	 * clears everything. 
	 * 
	 * @return		true if successful.
	 * @throws ConnectException
	 */
	public boolean clear() throws ConnectException
	{
		String retval=sendExpression("clear()");
		return retval.contains("true");
	}
	
	/**
	 * print class definition.
	 * 
	 * @param classname		classname to print.
	 * @return				a string containing class definition.
	 * @throws ConnectException
	 */
	public String list(String classname) throws ConnectException
	{
		String retval=sendExpression("list("+classname+")");
		return retval;
	}
	
	/**
	 * returns a list of parameter names in a class.
	 * 
	 * @param classname		class name which we want to get the parameter names from.
	 * @return				a list of parameter names.
	 * @throws ConnectException
	 */
	public String[] getParameterNames(String classname) throws ConnectException
	{
		String retvaltemp=sendExpression("getParameterNames("+classname+")");
		return retvaltemp.substring(1, retvaltemp.length()-2).split(",");
	}
	
	/**
	 * sets the parameter value of paramname in classname.
	 * 
	 * @param classname		class name containing the parameter.
	 * @param paramname		parameter name.
	 * @param val			value of the parameter.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean setParameterValue(String classname, String paramname, 
			String val) throws ConnectException 
	{
		String retval=sendExpression("setParameterValue("+classname+","+
					paramname+","+val+")");
		return retval.contains("Ok");
	}
	
	/**
	 * gets the value of parameter paramname inside classname.
	 * 
	 * @param classname		class name containing the parameter.
	 * @param paramname		parameter name.
	 * @return				a String containing the parameter value.
	 * @throws ConnectException
	 */
	public String getParameterValue(String classname, String paramname) throws ConnectException
	{
		String retval=sendExpression("getParameterValue("+classname+","+paramname+")");
		return retval;
	}

	/**
	 * simulate the model.
	 * 
	 * @param modelname		model name.
	 * @param tstart		startTime.
	 * @param tstop			stopTime.
	 * @param method		method={"dassl","dassl2","euler"}
	 * @param noofinterval	numberOfIntervals
	 * @return
	 * @throws ConnectException
	 */
	public boolean simulate(String modelname,String tstart, String tstop, String method) throws ConnectException
	{
		String retval=sendExpression("simulate("+modelname+",startTime="+tstart+
					",stopTime="+tstop+",method=\""+method+"\")");
		return retval.contains("plt");
	}
	
	/**
	 * simulate the model.
	 * 
	 * @param modelname		model name.
	 * @param tstart		startTime.
	 * @param tstop			stopTime.
	 * @param method		method={"dassl","dassl2","euler"}
	 * @param noofinterval	numberOfIntervals
	 * @return
	 * @throws ConnectException
	 */
	public boolean simulate(String modelname, String tstart, String tstop, String method, 
			String noofinterval) throws ConnectException
	{
		String retval=sendExpression("simulate("+modelname+",startTime="+tstart+
					",stopTime="+tstop+",method=\""+method+
					"\",numberOfIntervals="+noofinterval+")");
		return retval.contains("plt");
	}
	
	/**
	 * 
	 */
	public boolean simulate() throws ConnectException {
//		simulate(className, [startTime], [stopTime], [numberOfIntervals], [tolerance], [method], [fileNamePrefix], [options], [outputFormat], [variableFilter], [cflags], [simflags])
		
		return false;
	}
	
	/**
	 * plot using OpenModelica plotting function.
	 * 
	 * @param vartoplot		variables to plot, e.g. "{a,b}"
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean plotOM(String vartoplot) throws ConnectException
	{
		String retval=sendExpression("plot("+vartoplot+")");
		return retval.contains("true");
	}
	
	/**
	 * set the file where the model will be saved into using save command.
	 * 
	 * @param modelname		model name to be link to the file.
	 * @param fname			file name which the model is to be saved.
	 * @return				true if successful.
	 * @throws ConnectException
	 */
	public boolean setSourceFile(String modelname, String fname) throws ConnectException
	{
		String retval=sendExpression("setSourceFile("+modelname+",\""+fname+"\")");
		return retval.contains("Ok");
	}

	public boolean setModelicaLibraryPath(String path) throws ConnectException{
		String retval=sendExpression("setEnvironmentVar(\"OPENMODELICALIBRART\",\""+path+"\")");
		return retval.contains("Ok");
	}
	
	/**
	 * @author Marc Sabate
	 * Gets the value of a variable at time t
	 * @param variableName
	 * @param t
	 * @throws ConnectException 
	 */
	public String getValue(String variableName, String t) throws ConnectException
	{
		String retval = sendExpression("val("+variableName+","+t+")");
		return retval;
	}

	/**
	 * @author Silvia Machado
	 * Gets results variables and values from a simulation file
	 * @param fileName
	 * @param readParameters
	 * @param openmodelicaStyle
	 * @throws ConnectException
	 */
	public String getSimulationVars(String fileName, boolean readParameters, boolean openmodelicaStyle) throws ConnectException {
		return sendExpression("readSimulationResultVars(\"" + fileName + "\"," + readParameters + "," + openmodelicaStyle + ")");
		
	}
	
	public String getSimulationVars(String fileName) throws ConnectException {
		return getSimulationVars(fileName, true, false);
	}
	
	/**
	 * @author Silvia Machado
	 * Gets the selected variables from the simulation results producing the output file.
	 * @param fileName
	 * @param readParameters
	 * @param openmodelicaStyle
	 * @throws ConnectException
	 */
	public String filterSimulationResults(String inFileName, String outFileName, List<String> varsList, int numberOfIntervals) throws ConnectException {
		String vars = varsList.toString();
		String retval = sendExpression("filterSimulationResults(" + inFileName + "," + outFileName + "," + vars + "," + numberOfIntervals + ")");
		return retval;
	}
	
	public String filterSimulationResults(String inFileName, String outFileName, List<String> varsList) throws ConnectException {
		return filterSimulationResults(inFileName, outFileName, varsList, 0);
	}
	
	public String readSimulationResultSize(String fileName) throws ConnectException {
		return sendExpression("readSimulationResultSize(" + fileName + ")");
	}
	
		/**
	 * @author Silvia Machado
	 * Returns a user-friendly string containing the errors stored in the buffer. With warningsAsErrors=true, it reports warnings as if they were errors.
	 * @param warningAsErrors
	 * @throws ConnectException 
	 */
	public String getErrorString(boolean warningsAsErrors) throws ConnectException {
		return sendExpression("getErrorString(" + warningsAsErrors + ")");
	}
	
	public String getErrorString() throws ConnectException {
		return sendExpression("getErrorString(" + false + ")");
	}

}
