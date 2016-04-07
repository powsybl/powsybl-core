/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp_exp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public class ModelicaFileSplitter extends ModelicaHierarchyExtractor
{
	public ModelicaFileSplitter(File folder)
	{
		this.folder = folder;
	}

	@Override
	public void onStartClass(String specifier, String ident, String comment, boolean isComposition, String line)
	{
		super.onStartClass(specifier, ident, comment, isComposition, line);

		// Only start new files on class that are not groups (models, records, classes, but not packages)
		// Functions do not start new files
		if (isStartOfSplit()) startSplitFile();
	}

	@Override
	public void onEndClass(String ident)
	{
		if (isEndOfSplit()) endSplitFile();
		super.onEndClass(ident);
	}

	@Override
	public void onLine(String line)
	{
		super.onLine(line);
		if (writer == null) return;
		
		try
		{
			writer.write(line);
			writer.newLine();
		}
		catch (IOException x)
		{
			log.error("onLine, writing line [" + line + "]");
		}
	}

	boolean isStartOfSplit()
	{
		ModelicaHierarchy.Item current = getHierarchy().getStack().peek();
		// Consider if the current context of hierarchy must go on a separate file

		// Packages do not start split files
		if (current.isClassGroup) return false;

		// If we have decided to split contents to a file, send all output to that file until
		// we receive the end of the item that caused the split (the hierarchy item is ended)
		if (writer != null) return false;

		return true;
	}

	boolean isEndOfSplit()
	{
		ModelicaHierarchy.Item currentItem = getHierarchy().getStack().peek();
		return currentItem == splitItem;
	}

	void startSplitFile()
	{
		ModelicaHierarchy h = getHierarchy();
		splitItem = h.getStack().peek();

		try
		{
			// Close current writer
			if (writer != null) writer.close();

			// Create the new writer
			String prefix = h.getQualifiedName() + ".";
			String suffix = ".mo";
			// We use createTempFile to avoid over-writing files if only difference in qualified name is case (Windows, Mac OS, ...)
			writer = new BufferedWriter(new FileWriter(File.createTempFile(prefix, suffix, folder)));

			// Write header for current element on top of hierarchy
			for (Iterator<ModelicaHierarchy.Item> k = h.stack.descendingIterator(); k.hasNext();)
			{
				ModelicaHierarchy.Item item = k.next();

				// Only write groups
				if (!item.isClassGroup) continue;

				// We could write the original line (including description/comments)
				// or a simplified version with only class specifier and identifier
				if (OUTPUT_ORIGINAL_CLASS_SPECIFIER_LINES)
				{
					writer.write(item.line);
				}
				else
				{
					writer.write(item.getIndentation());
					writer.write(item.specifier);
					writer.write(" ");
					writer.write(item.ident);
				}
				writer.newLine();
			}
		}
		catch (IOException x)
		{
			log.error("startSplitFile for [" + h.getQualifiedName() + "]");
		}
	}

	void endSplitFile()
	{
		splitItem = null;

		if (writer == null)
		{
			log.error("endSplitFile, no current writer");
			return;
		}
		try
		{
			// Write footer, close all elements of current point in hierarchy
			for (Iterator<ModelicaHierarchy.Item> k = getHierarchy().getStack().iterator(); k.hasNext();)
			{
				ModelicaHierarchy.Item item = k.next();

				if (!item.isClassGroup) continue;

				writer.write(item.getIndentation());
				writer.write(ModelicaGrammar.END);
				writer.write(" ");
				writer.write(item.ident);
				writer.write(";");
				writer.newLine();
			}
			writer.close();
			writer = null;
		}
		catch (IOException x)
		{
			log.error("endSplitFile for " + getHierarchy().getQualifiedName() + "]");
		}
	}

	File						folder;
	ModelicaHierarchy.Item		splitItem								= null;
	BufferedWriter				writer									= null;

	static final boolean		OUTPUT_ORIGINAL_CLASS_SPECIFIER_LINES	= true;

	static final Logger log										= LoggerFactory.getLogger(ModelicaFileSplitter.class);
}