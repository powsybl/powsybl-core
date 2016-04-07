/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp_exp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A Modelica partial grammar
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public class ModelicaGrammar
{
	public static CommentScanner newCommentScanner(CommentScannerEventHandler e)
	{
		CommentScanner s = new CommentScanner(e);
		s.setQuote(ModelicaGrammar.QUOTE_REGEX);
		s.setLineComment(ModelicaGrammar.LINE_COMMENT_REGEX);
		s.setBlockComment(ModelicaGrammar.BLOCK_COMMENT_START_REGEX, ModelicaGrammar.BLOCK_COMMENT_END_REGEX);
		return s;
	}

	public static boolean isClassQualifier(String keyword)
	{
		return belongs(keyword, CLASS_QUALIFIERS);
	}

	public static boolean isClassSpecifier(String keyword)
	{
		return belongs(keyword, CLASS_SPECIFIERS);
	}

	public static boolean isStringType(String type)
	{
		return type.equals(BUILTIN_TYPE_STRING);
	}

	public static boolean isRealType(String type)
	{
		return type.equals(BUILTIN_TYPE_REAL);
	}

	public static boolean isIntegerType(String type)
	{
		return type.equals(BUILTIN_TYPE_INTEGER);
	}

	public static boolean isNumberType(String type)
	{
		return type.equals(BUILTIN_TYPE_REAL) || type.equals(BUILTIN_TYPE_INTEGER);
	}

	public static boolean isBooleanType(String type)
	{
		return type.equals(BUILTIN_TYPE_BOOLEAN);
	}

	public static boolean isNumber(String value)
	{
		return NUMBER_REGEX.matcher(value).matches();
	}

	public static boolean isBoolean(String value)
	{
		return value.equals(ModelicaGrammar.TRUE) || value.equals(ModelicaGrammar.FALSE);
	}

	// Scanning delimiters
	// A quote at the beginning of the line or a quote not preceded by a backslash
	static final Pattern		QUOTE_REGEX						= Pattern.compile("^\\\"|[^\\\\]\\\"");
	// Comments
	static final String			LINE_COMMENT					= "//";
	static final String			BLOCK_COMMENT_START				= "/*";
	static final String			BLOCK_COMMENT_END				= "*/";
	static final char			EQ								= '=';
	static final char			SEMICOLON						= ';';
	static final Pattern		LINE_COMMENT_REGEX				= Pattern.compile(Pattern.quote(LINE_COMMENT));
	static final Pattern		BLOCK_COMMENT_START_REGEX		= Pattern.compile(Pattern.quote(BLOCK_COMMENT_START));
	static final Pattern		BLOCK_COMMENT_END_REGEX			= Pattern.compile(Pattern.quote(BLOCK_COMMENT_END));

	// Class specifiers
	static final String 		PACKAGE 						= "package";
	static final Set<String>	CLASS_QUALIFIERS				= new HashSet<String>(Arrays.asList("encapsulated", "partial", "expandable"));
	static final Set<String>	CLASS_SPECIFIERS				= new HashSet<String>(Arrays.asList("class", "model", "record", "block", "connector", "type", PACKAGE, "function", "operator"));
	static final Set<String>	CLASS_GROUP_SPECIFIERS			= new HashSet<String>(Arrays.asList("package"));
	static final String			END								= "end";
	static final Set<String>	ENDS_OF_NON_CLASS_SPECIFIERS	= new HashSet<String>(Arrays.asList("if", "while", "for", "when"));

	// Parameters
	static final String			PARAMETER						= "parameter";
	static final Pattern		PARAMETER_TYPE_REGEX			= Pattern.compile("\\s*" + Pattern.quote(PARAMETER) + "\\s*(?<type>\\S+)\\s*");
	static final char			CLASS_MODIFICATION_START		= '(';
	static final char			CLASS_MODIFICATION_END			= ')';
	static final String			BUILTIN_TYPE_REAL				= "Real";
	static final String			BUILTIN_TYPE_INTEGER			= "Integer";
	static final String			BUILTIN_TYPE_STRING				= "String";
	static final String			BUILTIN_TYPE_BOOLEAN			= "Boolean";
	static final Set<String>	BUILTIN_TYPES					= new HashSet<String>(Arrays.asList(BUILTIN_TYPE_REAL, BUILTIN_TYPE_INTEGER, BUILTIN_TYPE_BOOLEAN, BUILTIN_TYPE_STRING));
	static final String			TRUE							= "true";
	static final String			FALSE							= "false";
	static final Pattern		NUMBER_REGEX					= Pattern.compile("[-+]?\\s*\\d+([\\.]\\d+)?([eE][-+]?\\d+)?");

	static boolean isClassGroupSpecifier(String keyword)
	{
		return belongs(keyword, CLASS_GROUP_SPECIFIERS);
	}

	static boolean isEndForNonClassSpecifier(String keyword)
	{
		return belongs(keyword, ENDS_OF_NON_CLASS_SPECIFIERS);
	}

	static boolean isBuiltinType(String keyword)
	{
		return belongs(keyword, BUILTIN_TYPES);
	}

	static boolean belongs(String keyword, Set<String> keywords)
	{
		if (keyword == null) return false;
		if (keyword.equals("")) return false;
		return keywords.contains(keyword);
	}
}