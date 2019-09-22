package com.topica.edu.itlab.jdbc.util;

import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class keep infomation about table
 * @author ljnk975
 */
class TableRef {

	/**
	 * Name of table
	 */
	private String name;

	/**
	 * Primary key
	 */
	private ColumnRef idKey;

	/**
	 * Column in that table
	 */
	private List<ColumnRef> listOfColumn;

	/**
	 * Select name
	 */
	private String selectName;

	/**
	 * Class reference
	 */
	private Class<?> aClass;

	/**
	 * Create table object
	 * @param selectName select name in query
	 * @param tabName    table name
	 */
	TableRef(String selectName, String tabName, Class<?> aClass) {
		this.aClass = aClass;
		this.selectName = selectName;
		this.name = tabName;
		this.listOfColumn = new ArrayList<ColumnRef>();
	}

	/**
	 * @return the selectName
	 */
	public final String getSelectName() {
		return selectName;
	}

	/**
	 * @param selectName the selectName to set
	 */
	public final void setSelectName(String selectName) {
		this.selectName = selectName;
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
	 * Add column
	 * @param col the column
	 */
	public final void addColumn(ColumnRef col) {
		this.listOfColumn.add(col);
	}

	/**
	 * Get iterator of column
	 * @return iterator of column
	 */
	public final Iterator<ColumnRef> getColumnIterator() {
		return this.listOfColumn.iterator();
	}

	/**
	 * @return the idKey
	 */
	public final ColumnRef getIdKey() {
		return idKey;
	}

	/**
	 * @param idKey the idKey to set
	 */
	public final void setIdKey(ColumnRef idKey) {
		this.idKey = idKey;
	}

	/**
	 * @return the aClass
	 */
	public final Class<?> getClassRef() {
		return aClass;
	}

	/**
	 * Get relative col one to many
	 * @param tabName name of table
	 * @return relative col
	 */
	public final ColumnRef getRelativeColumn(String tabName) {
		Iterator<ColumnRef> ierator = this.getColumnIterator();
		while (ierator.hasNext()) {
			ColumnRef col = ierator.next();

			// check mapped name
			if (col.getMappedBy() != null) {
				if (col.getMappedBy().equalsIgnoreCase(tabName))
					return col;
			}
		}

		// not found return null
		return null;
	}

	/**
	 * Get object of next query<br/>
	 * If object contain in list then it will create new one, fetch data and add to list
	 * @param ret  resultset from select query sql
	 * @param list list of object have been create
	 * @param calledObj Object call mapped
	 * @return     object
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InstantiationException 
	 * @throws LoaderException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final Object getObject(ResultSet ret, List<?> list, Object calledObj) throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException, LoaderException {
		Iterator<?> ierator = list.iterator();
		while (ierator.hasNext()) {
			Object obj = ierator.next();

			// check equals object
			if (this.idKey != null && this.idKey.getField().get(obj).equals(ret.getLong(this.idKey.getSelectName()))) {
				// foreach column
				Iterator<ColumnRef> columnIterator = this.getColumnIterator();
				while (columnIterator.hasNext()) {
					ColumnRef col = columnIterator.next();

					// if mapped by
					if (col.getMappedBy() != null) {
						// call mapped
						col.getMappedTab().getObject(ret, (List<?>)col.getField().get(obj), obj);
					}
				}
				return obj;
			}
		}

		// not found create new
		Object obj = this.aClass.newInstance();

		// foreach column
		Iterator<ColumnRef> columnIterator = this.getColumnIterator();
		while (columnIterator.hasNext()) {
			ColumnRef col = columnIterator.next();

			// if field can't access then set access
			if (Modifier.isPrivate(col.getField().getModifiers()))
				col.getField().setAccessible(true);

			// if mapped by
			if (col.getMappedBy() != null) {
				// create new list and call mapped to next table
				List list2 = new ArrayList();
				col.getField().set(obj, list2);

				// call mapped
				col.getMappedTab().getObject(ret, list2, obj);
			}
			// if is many to one
			else if (col.getRelative() != null)
				col.getField().set(obj, calledObj);
			// type of string
			else if (col.getField().getType() == String.class)
				col.getField().set(obj, ret.getString(col.getSelectName()));
			// type of boolean
			else if (col.getField().getType() == boolean.class || col.getField().getType() == Boolean.class)
				col.getField().set(obj, ret.getBoolean(col.getSelectName()));
			// type byte
			else if (col.getField().getType() == byte.class || col.getField().getType() == Byte.class)
				col.getField().set(obj, ret.getByte(col.getSelectName()));
			// type short
			else if (col.getField().getType() == short.class || col.getField().getType() == Short.class)
				col.getField().set(obj, ret.getShort(col.getSelectName()));
			// type int
			else if (col.getField().getType() == int.class || col.getField().getType() == Integer.class)
				col.getField().set(obj, ret.getInt(col.getSelectName()));
			// type long
			else if (col.getField().getType() == long.class || col.getField().getType() == Long.class)
				col.getField().set(obj, ret.getLong(col.getSelectName()));
			// Can't recognize type
			else
				throw new LoaderException("Can't recognize type of field: "+col.getField().getName());
		}

		// Add to list
		((List<Object>)list).add(obj);

		// return it
		return obj;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		return this.name.equals(((TableRef)obj).name);
	}

}
