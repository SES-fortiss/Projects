package org.fortiss.smg.sqltools.lib.builder;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;

public class DeleteQuery {

	private final String table;
	private Collection<String> wheres;

	public DeleteQuery(String table) {
		this.table = table;
		wheres = new LinkedList<String>();
	}

	public DeleteQuery addWhere(String where) {
		wheres.add(where);
		return this;
	}

	public DeleteQuery and(String where) {
		return addWhere("AND " + where);
	}

	public DeleteQuery or(String where) {
		return addWhere("OR " + where);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("DELETE FROM ")
			  .append(table);

		if (!wheres.isEmpty()) {
			result.append(" WHERE ")
				  .append(StringUtils.join(wheres, " "));
		}

		return result.toString();
	}

}