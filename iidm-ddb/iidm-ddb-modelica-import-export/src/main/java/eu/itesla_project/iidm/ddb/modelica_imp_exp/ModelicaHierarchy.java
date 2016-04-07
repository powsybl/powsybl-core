/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.modelica_imp_exp;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * @author Luis Maria Zamarreno <zamarrenolm@aia.com>
 * @author Silvia Machado <machados@aia.es>
 */
public class ModelicaHierarchy
{
	public Deque<ModelicaHierarchy.Item> getStack()
	{
		return stack;
	}

	public String getQualifiedName()
	{
		StringBuilder qname = new StringBuilder(16 * stack.size());
		for (Iterator<ModelicaHierarchy.Item> k = stack.descendingIterator(); k.hasNext();)
		{
			ModelicaHierarchy.Item item = k.next();
			if (qname.length() == 0) qname.append(item.ident);
			else qname.append(".").append(item.ident);
		}
		return qname.toString();
	}

	public static class Item
	{
		Item(String specifier, String ident, String comment, boolean isComposition, String line)
		{
			this.specifier = specifier;
			this.isComposition = isComposition;
			this.ident = ident;
			this.comment = comment;
			this.line = line;
			this.isClassGroup = ModelicaGrammar.isClassGroupSpecifier(specifier);
		}

		public String getSpecifier()
		{
			return specifier;
		}

		public String getIdent()
		{
			return ident;
		}
		
		public String getComment()
		{
			return comment;
		}

		public boolean isComposition()
		{
			return isComposition;
		}

		public String getLine()
		{
			return line;
		}

		void setLevel(int level)
		{
			this.level = level;
		}

		public int getLevel()
		{
			return level;
		}

		public String getIndentation()
		{
			return ModelicaHierarchy.getIndentation(level);
		}

		final String	specifier;
		final String	ident;
		final String	comment;
		final boolean	isComposition;
		final String	line;
		final boolean	isClassGroup;
		int				level;
	}

	Deque<ModelicaHierarchy.Item>	stack	= new ArrayDeque<ModelicaHierarchy.Item>();

	void push(ModelicaHierarchy.Item item)
	{
		stack.addFirst(item);
		item.setLevel(getLevel());
	}

	ModelicaHierarchy.Item pop()
	{
		return stack.removeFirst();
	}

	int getLevel()
	{
		return stack.size() - 1;
	}

	static String getIndentation(int level)
	{
		String indentation = "";
		for (int k = 0; k < level; k++)
			indentation += INDENTATION_BLOCK;
		return indentation;
	}

	private static final String	INDENTATION_BLOCK	= "     ";
}
