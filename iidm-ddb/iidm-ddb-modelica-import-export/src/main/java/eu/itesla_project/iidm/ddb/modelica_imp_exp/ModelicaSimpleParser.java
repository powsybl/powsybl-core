/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp_exp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;

/**
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public class ModelicaSimpleParser implements CommentScannerEventHandler
{
	public ModelicaSimpleParser(ModelicaParserEventHandler eventHandler)
	{
		this.eventHandler = eventHandler;
		commentScanner = new CommentScanner(this);
		commentScanner.setQuote(ModelicaGrammar.QUOTE_REGEX);
		commentScanner.setLineComment(ModelicaGrammar.LINE_COMMENT_REGEX);
		commentScanner.setBlockComment(ModelicaGrammar.BLOCK_COMMENT_START_REGEX, ModelicaGrammar.BLOCK_COMMENT_END_REGEX);
	}

	public void parse(File file) throws IOException
	{
		initFile(file);
		eventHandler.onStartFile(file);
		commentScanner.scan(file);
		eventHandler.onEndFile(file);
	}

	void initFile(File file)
	{
		isPendingEndClass = false;
		pendingEndClassIdent = null;
	}

	void parseLine(String line)
	{
		initLine(line);

		// We will only try to parse class specifiers and parameters
		tryParseClassSpecifier();
		tryParseEndClassSpecifier();
		tryParseParameterSpecifier();

		// Emit events for the found elements
		if (isClassSpecifier) emitStartClass();
		if (isParameter) eventHandler.onParameter(parameter);
		eventHandler.onLine(originalLine);
		if (isEndClassSpecifier) emitEndClass();
	}

	void emitStartClass()
	{
		if (isPendingEndClass)
		{
			eventHandler.onEndClass(pendingEndClassIdent);
			isPendingEndClass = false;
			pendingEndClassIdent = null;
		}
		eventHandler.onStartClass(specifier, ident, comment, isComposition, originalLine);

		// If this is not a composition, hold the class identifier to be able to emit it later when a new class specifier is found
		// This way, all the lines between the class specifier for classA (not a composition) and a new class specifier for classB
		// are considered part of the definition of classA
		// Example:
		// record Genrou_data =
		// PowerSystems.Electrical.Machines.PSSE.DATA.GENROU (...)
		// record Gencls_data
		// ...
		// end Gendcls_data;
		// In the example,
		// The call to GENROU is a line of text that must be considered inside the definition of record Genrou_data
		if (!isComposition)
		{
			isPendingEndClass = true;
			pendingEndClassIdent = ident;
		}
	}
	
	void emitEndClass()
	{
		if (isPendingEndClass)
		{
			eventHandler.onEndClass(pendingEndClassIdent);
			isPendingEndClass = false;
			pendingEndClassIdent = null;
		}
		eventHandler.onEndClass(ident);
	}

	void initLine(String line)
	{
		isClassSpecifier = false;
		isComposition = false;
		isEndClassSpecifier = false;
		specifier = null;
		ident = null;
		isParameter = false;
		parameter = null;

		parsingLine = line;
		tokens = line.trim().split("\\s+");

		log.debug("LINE   [" + line + "]");
		log.debug("TOKENS " + Arrays.toString(tokens));
	}

	void tryParseClassSpecifier()
	{
		int k;

		// First ignore optional class qualifiers
		for (k = 0; k < tokens.length; k++)
		{
			if (!ModelicaGrammar.isClassQualifier(tokens[k])) break;
		}
		// Then check if we have found a class qualifier
		if (ModelicaGrammar.isClassSpecifier(tokens[k]))
		{
			isClassSpecifier = true;
			// The keyword package is a class specifier and a class group specifier
			specifier = tokens[k];
			k++;
			ident = tokens[k];
			k++;
			if (k >= tokens.length || !tokens[k].equals("=")) isComposition = true;
			
			// Assume the first quoted in the line is the comment
			comment = getFirstQuoted(parsingLine, 0);

			log.debug("Class specifier, specifier = " + specifier + ", ident = " + ident + ", isComposition = " + isComposition + ", comment = " + comment);
		}
	}

	void tryParseEndClassSpecifier()
	{
		if (tokens.length < 2) return;
		String whatEnds = tokens[1].replace(";", "");
		isEndClassSpecifier = tokens[0].equals(ModelicaGrammar.END) && !ModelicaGrammar.isEndForNonClassSpecifier(whatEnds);
		if (isEndClassSpecifier)
		{
			specifier = ModelicaGrammar.END;
			ident = whatEnds;
		}
	}

	void tryParseParameterSpecifier()
	{
		// We will only parse parameter specifiers according to the following rules:
		// Completely defined in a single line
		// Line contains at least three tokens
		// Two first tokens in the line must be keyword "parameter" and an allowed built-in type
		// Only for built-in types Real, Integer, Boolean, String
		// Values are direct constants, not expressions or references to other objects
		// We try to ignore class modifications that come in parenthesis after the name
		// Values will be checked against identified built-in type

		ModelicaParameter param = new ModelicaParameter();
		if (!isParameterSpecifier()) return;
		if (!checkParameterBuiltinType(param)) return;

		// Rest of the line is parsed without taking into account default tokenization
		// We move through the line text using an index
		int p = skipParameterType(param);
		if (p < 0) return;
		p = checkParameterName(param, p);
		if (p < 0) return;
		if (!checkParameterValue(param, p)) return;

		isParameter = true;
		parameter = param;
	}

	boolean isParameterSpecifier()
	{
		return tokens.length >= 3 && tokens[0].equals(ModelicaGrammar.PARAMETER);
	}

	boolean checkParameterBuiltinType(ModelicaParameter param)
	{
		String type = tokens[1];
		if (ModelicaGrammar.isBuiltinType(type))
		{
			param.type = type;
			return true;
		}
		return false;
	}

	int skipParameterType(ModelicaParameter param)
	{
		// We start to process at the end of the "parameter <built-in type>" string
		Matcher matcher = ModelicaGrammar.PARAMETER_TYPE_REGEX.matcher(parsingLine);
		int p = 0;
		if (matcher.find())
		{
			p = matcher.end();
			String expected = param.type;
			String found = matcher.group("type");
			if (!expected.equals(found))
			{
				log.warn("Parameter specifier, type mismatch " + expected + " != " + found + " analyzing line " + parsingLine);
				return -1;
			}
		}
		return p;
	}

	int checkParameterName(ModelicaParameter param, int p)
	{
		// Name is obtained from current line at current position
		// Ignoring a potential parenthesized expression (class modification)
		// And up to an equal character, trimming trailing blanks

		int sp = parsingLine.indexOf(ModelicaGrammar.CLASS_MODIFICATION_START, p);
		int eq = parsingLine.indexOf(ModelicaGrammar.EQ, p);
		// The parenthesis happens before the first equal sign, skip class modification
		if (sp >= 0 && sp < eq)
		{
			param.name = parsingLine.substring(p, sp);
			p = parsingLine.indexOf(ModelicaGrammar.CLASS_MODIFICATION_END, sp + 1);
			if (p < 0)
			{
				log.warn("Parameter specifier. Unbalanced parenthesis in class modification, line [" + parsingLine + "]");
				return -1;
			}
			// Look for first equal sign after parenthesis
			eq = parsingLine.indexOf(ModelicaGrammar.EQ, p);
		}
		if (eq < 0)
		{
			log.debug("Parameter specifier. Missing equal in line [" + parsingLine + "]");
			return -1;
		}
		if (param.name == null)
		{
			param.name = parsingLine.substring(p, eq);
		}
		param.name = param.name.trim();
		p = eq + 1;
		return p;
	}

	boolean checkParameterValue(ModelicaParameter param, int p)
	{
		if (ModelicaGrammar.isStringType(param.type)) return checkParameterValueQuoted(param, p);
		String value = extractParameterValue(p);
		// Check that value is a proper value (a constant, not an expression)
		if (ModelicaGrammar.isBooleanType(param.type) && !(value.equals(ModelicaGrammar.TRUE) || value.equals(ModelicaGrammar.FALSE)))
		{
			log.debug("Parameter specifier. Wrong value [" + value + "] for Boolean in line [" + parsingLine + "]");
			return false;
		}
		else if (ModelicaGrammar.isNumberType(param.type) && !ModelicaGrammar.NUMBER_REGEX.matcher(value).matches())
		{
			log.debug("Parameter specifier. Wrong value (not a number) [" + value + "] for " + param.type + " in line [" + parsingLine + "]");
			return false;
		}
		param.value = value;
		return true;
	}

	boolean checkParameterValueQuoted(ModelicaParameter param, int p)
	{
		String quoted = getFirstQuoted(parsingLine, p);
		if (quoted == null) return false;
		param.value = quoted;
		return true;
	}
		
	static String getFirstQuoted(String line, int p)
	{
		Matcher q = ModelicaGrammar.QUOTE_REGEX.matcher(line);
		if (!q.find(p))
		{
			log.debug("Get first quoted. Missing quoted start in line [" + line + "]");
			return null;
		}

		int qs = q.end();
		int restart = (qs > 1 ? qs - 1 : qs);
		if (!q.find(restart))
		{
			log.debug("Get first quoted. Missing quoted end in line [" + line + "]");
			return null;
		}

		int qe = q.start() + (q.start() > 0 ? 1 : 0);
		return line.substring(qs, qe);
	}
	
	String extractParameterValue(int p)
	{
		// The end of the value field should be delimited by a quote (formal comment) or a semicolon
		int ev = parsingLine.indexOf('"', p);
		if (ev < 0) ev = parsingLine.indexOf(';', p);
		// Assume value is the rest of the line
		if (ev < 0) ev = parsingLine.length();

		return parsingLine.substring(p, ev).trim();
	}

	@Override
	public void onQuoted(String quoted, boolean open, boolean close)
	{
		// For multi line strings we will preserve in the parsing line only the contents of the first line
		// (the text from the open quote to the end of the line)
		// Intermediate lines and quoted text of last line will be removed from the parsing line
		// If we would like to preserve all text we could open-close every line but add "+" after each line except the last one
		// Both approaches solve the problem of finding class specifier keywords inside a quoted text
		boolean preserve = (open && close) || (open && !close);
		if (preserve)
		{
			parsingLineBuilder.append('"');
			parsingLineBuilder.append(quoted);
			parsingLineBuilder.append('"');
		}
	}

	@Override
	public void onLineComment(String lineComment)
	{
		// Ignore comments for line prepared to the parser (they are still maintained in the original line)
	}

	@Override
	public void onBlockComment(String blockComment, boolean open, boolean close)
	{
		// Ignore comments for line prepared to the parser (they are still maintained in the original line)
	}

	@Override
	public void onText(String text)
	{
		parsingLineBuilder.append(text);
	}

	@Override
	public void onLineScanned(String originalLine)
	{
		this.originalLine = originalLine;
		String line = parsingLineBuilder.toString();
		parsingLineBuilder.delete(0, parsingLineBuilder.length());
		parseLine(line);
	}

	final ModelicaParserEventHandler	eventHandler;
	final CommentScanner				commentScanner;
	// Receive text from comment scanner line by line
	final StringBuilder					parsingLineBuilder	= new StringBuilder(4096);

	String								originalLine;
	String								parsingLine;
	String[]							tokens;

	// Start-end of classes
	boolean								isPendingEndClass;
	boolean								isClassSpecifier;
	boolean								isComposition;
	boolean								isEndClassSpecifier;
	String								specifier;
	String								ident;
	String								comment;
	String								pendingEndClassIdent;

	// Parameters
	boolean								isParameter;
	ModelicaParameter					parameter;

	static final Logger log					= LoggerFactory.getLogger(ModelicaSimpleParser.class);
}