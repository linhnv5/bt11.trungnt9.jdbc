package com.topica.edu.itlab.jdbc.util;

import java.lang.reflect.Field;

/**
 * Class contain information about column in table
 * @author ljnk975
 */
class ColumnRef {

	/**
	 * Table reference
	 */
	private TableRef table;

	/**
	 * Name of column, null if it is a many to one relative
	 */
	private String name;

	/**
	 * Field reference
	 */
	private Field field;

	/**
	 * Relative column, many to one relative
	 */
	private ColumnRef relative;

	/**
	 * Mapped by one to many relative, null if not one to many relative
	 */
	private String mappedBy;

	/**
	 * Mapped table, one to many relative, null if not one to many relative
	 */
	private TableRef mappedTab;

	/**
	 * Create a column
	 * @param tab
	 * @param name
	 * @param mappedBy
	 * @param field
	 */
	ColumnRef(TableRef tab, String name, String mappedBy, Field field) {
		this.table = tab;
		this.field = field;
		this.name  = name;
		this.mappedBy = mappedBy;
	}

	/**
	 * @return the table
	 */
	public final TableRef getTable() {
		return table;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the selectName
	 */
	public final String getSelectName() {
		return table.getSelectName()+"."+this.name;
	}
	
	/**
	 * @return the relative
	 */
	public final ColumnRef getRelative() {
		return relative;
	}

	/**
	 * @param relative the relative to set
	 */
	public final void setRelative(ColumnRef relative) {
		this.relative = relative;
	}

	/**
	 * @return the mappedBy
	 */
	public final String getMappedBy() {
		return mappedBy;
	}

	/**
	 * @return the field
	 */
	public final Field getField() {
		return field;
	}

	/**
	 * @return the mappedTab
	 */
	public final TableRef getMappedTab() {
		return mappedTab;
	}

	/**
	 * @param mappedTab the mappedTab to set
	 */
	public final void setMappedTab(TableRef mappedTab) {
		this.mappedTab = mappedTab;
	}

}
