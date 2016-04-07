/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp_exp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public class CommentScanner
{
	public CommentScanner(CommentScannerEventHandler eventHandler)
	{
		this.eventHandler = eventHandler;
	}

	public void setQuote(Pattern quote)
	{
		this.quoteRegex = quote;
	}

	public void setLineComment(Pattern lineComment)
	{
		this.lineCommentRegex = lineComment;
	}

	public void setBlockComment(Pattern blockCommentStart, Pattern blockCommentEnd)
	{
		this.blockCommentStartRegex = blockCommentStart;
		this.blockCommentEndRegex = blockCommentEnd;
	}

	public void scan(File file) throws IOException
	{
		if (!isInitialized())
		{
			log.error("Not initialized");
			return;
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null)
		{
			scan(line);
			eventHandler.onLineScanned(line);
		}
		reader.close();
	}

	boolean isInitialized()
	{
		return quoteRegex != null && lineCommentRegex != null && blockCommentStartRegex != null && blockCommentEndRegex != null;
	}

	void scan(String line)
	{
		// We will allow comment characters inside a constant string
		// We will also quoted strings inside a comment
		// We will emit block comments that span multiple lines as a sequence of comment blocks, one for every line
		// Same for multiple line quoted strings

		Matcher quote = quoteRegex.matcher(line);
		Matcher lineComment = lineCommentRegex.matcher(line);
		Matcher blockCommentStart = blockCommentStartRegex.matcher(line);
		Matcher blockCommentEnd = blockCommentEndRegex.matcher(line);

		// Look for the line starting point, taking into account special situations where
		// we are inside a string, line really begins at first occurrence of quote character (if it occurs in the line)
		// we are inside a block comment, line really begins at first occurrence of block comment end (if it occurs in the line)
		int p = skipStart(line, quote, blockCommentEnd);

		// If after skipping the start we continue inside a block comment emit whole line as a block comment and finish
		// Similar if we are still (totally) inside a string, emit whole line as a string and finish scan
		if (isInsideBlockComment)
		{
			eventHandler.onBlockComment(line, false, false);
			return;
		}
		if (isInsideString)
		{
			eventHandler.onQuoted(line, false, false);
			return;
		}

		// Go across the line
		while (p < line.length())
		{
			MatchResult q, bcs, lc;

			// From current position find first of occurrence of {string start, block comment start, line comment start}
			q = find(quote, p);
			bcs = find(blockCommentStart, p);
			lc = find(lineComment, p);

			// Check what comes first
			boolean isQuote = q != null && (bcs == null || q.start() < bcs.start()) && (lc == null || q.start() < lc.start());
			boolean isBlockComment = bcs != null && (q == null || bcs.start() < q.start()) && (lc == null || bcs.start() < lc.start());
			boolean isLineComment = lc != null && (q == null || lc.start() < q.start()) && (bcs == null || lc.start() < bcs.start());

			if (isQuote)
			{
				// Emit as text from last pointer to here
				if (q.start() > 0) eventHandler.onText(line.substring(p, quoteStart(q)));
				p = skipQuoted(line, quote, q);
			}
			else if (isBlockComment)
			{
				// Emit as text from last pointer to here
				if (bcs.start() > 0) eventHandler.onText(line.substring(p, bcs.start()));
				p = skipBlockComment(line, blockCommentEnd, bcs);
			}
			else if (isLineComment)
			{
				if (lc.start() > 0) eventHandler.onText(line.substring(p, lc.start()));
				if (lc.end() < line.length()) eventHandler.onLineComment(line.substring(lc.end()));
				break;
			}
			else
			{
				// Emit the rest of the line as text and finish
				eventHandler.onText(line.substring(p));
				break;
			}
		}
	}

	MatchResult find(Matcher matcher, int p)
	{
		try
		{
			if (matcher.find(p)) return matcher.toMatchResult();
		}
		catch (IndexOutOfBoundsException x)
		{
			// Allow p out of range, will return null as result
		}
		return null;
	}

	int skipStart(String line, Matcher quote, Matcher blockCommentEnd)
	{
		MatchResult m = null;
		if (isInsideString) m = find(quote, 0);
		else if (isInsideBlockComment) m = find(blockCommentEnd, 0);

		int p = 0;
		if (m != null)
		{
			// If we were inside block comment emit a block comment, remove the flag
			if (isInsideBlockComment)
			{
				if (m.start() > 0) eventHandler.onBlockComment(line.substring(0, m.start()), false, true);
				isInsideBlockComment = false;
			}
			// If we were inside string emit a quoted string, remove the flag
			else if (isInsideString)
			{
				if (m.start() > 0) eventHandler.onQuoted(line.substring(0, quoteStart(m)), false, true);
				isInsideString = false;
			}
			p = m.end();
		}
		return p;
	}

	int skipQuoted(String line, Matcher quote, MatchResult q)
	{
		// First is a quote, skip until next (not escaped) quote is seen
		int p = q.end();
		int p1 = quoteRestart(q);
		MatchResult q1 = find(quote, p1);
		if (q1 == null)
		{
			// No more quotes in the line, we are inside a string not closed in this line
			eventHandler.onQuoted(line.substring(p), true, false);
			isInsideString = true;
			p = line.length();
		}
		else
		{
			// Emit this string and advance
			eventHandler.onQuoted(line.substring(p, quoteStart(q1)), true, true);
			p = q1.end();
		}
		return p;
	}

	int skipBlockComment(String line, Matcher blockCommentEnd, MatchResult bcs)
	{
		// We go through a block comment start and look for a block comment end
		int p = bcs.end();
		MatchResult bce = find(blockCommentEnd, p);
		if (bce == null)
		{
			// No block comment end found in this line, start a multi-line comment
			eventHandler.onBlockComment(line.substring(p), true, false);
			isInsideBlockComment = true;
			p = line.length();
		}
		else
		{
			// Emit this block comment and advance
			eventHandler.onBlockComment(line.substring(p, bce.start()), true, true);
			p = bce.end();
		}
		return p;
	}

	int quoteStart(MatchResult q)
	{
		// If quote match start is greater than zero it must have been matched by a non-quote + quote
		// The position of the quote character is adjusted
		return (q.start() > 0 ? q.start() + 1 : 0);
	}

	int quoteRestart(MatchResult q)
	{
		// If we want to restart the search for quotes from current result we must start search from end - 1
		// because the regular expression for searching quotes looks for sequence non-quote + quote
		// This fix allows to match empty quoted strings
		// Only when end > 1, re-starting from 1 is wrong
		return (q.end() > 1 ? q.end() - 1 : q.end());
	}

	boolean								isInsideString			= false;
	boolean								isInsideBlockComment	= false;

	final CommentScannerEventHandler	eventHandler;
	Pattern								quoteRegex, lineCommentRegex, blockCommentStartRegex, blockCommentEndRegex;

	static final Logger log						= LoggerFactory.getLogger(CommentScanner.class);
}
