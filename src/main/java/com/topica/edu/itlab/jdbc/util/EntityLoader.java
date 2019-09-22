package com.topica.edu.itlab.jdbc.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import com.topica.edu.itlab.jdbc.tutorial.annotation.Column;
import com.topica.edu.itlab.jdbc.tutorial.annotation.Id;
import com.topica.edu.itlab.jdbc.tutorial.annotation.JoinColumn;
import com.topica.edu.itlab.jdbc.tutorial.annotation.ManyToOne;
import com.topica.edu.itlab.jdbc.tutorial.annotation.OneToMany;
import com.topica.edu.itlab.jdbc.tutorial.annotation.Table;

/**
 * Utility class to load entity to memory
 * @author ljnk975
 */
public final class EntityLoader {

	/**
	 * Use lazy loading to load list entity from db to memory<br/>
	 * When use this, onetomany relative will not be loader
	 * @param aClass Define entity class to be loaded
	 * @param where  Where clause to define contraint to load data, if it's null then query don't have where clause
	 * @return       List of entity in database
	 * @throws LoaderException if there are some error while loading
	 * @throws SQLException    if there are some error while execute sql
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static final <E> List<E> lazyLoading(Class<E> aClass, String where, boolean printQuery) throws LoaderException, SQLException, InstantiationException, IllegalAccessException{
		int i; Table tab; Column col;

		// Create query
		final StringBuilder query = new StringBuilder("SELECT ");

		// Determine table name
		String tabName = (tab = aClass.getAnnotation(Table.class)) == null ? aClass.getName() : tab.name();

		// Map all field to loaded
		Map<String, Field> mapOfColumn = new HashMap<String, Field>();

		Field[] fields = aClass.getDeclaredFields();
		for (i = 0; i < fields.length; i++) {
			Field f = fields[i];

			// If is a column
			if ((col = f.getAnnotation(Column.class)) != null)
				mapOfColumn.put(col.name(), f);
		}

		// Put to query
		Iterator<String> columnIterator = mapOfColumn.keySet().iterator();
		if(!columnIterator.hasNext())
			throw new LoaderException("No column have been specified in class entity!");

		// Append first column
		query.append(columnIterator.next());

		// Append another column
		while (columnIterator.hasNext())
			query.append(", ").append(columnIterator.next());

		// append from table name
		query.append(" FROM ").append(tabName);

		// append where
		if (where != null)
			query.append(" WHERE ").append(where);

		// print query
		if (printQuery)
			System.out.println("Q: "+query);

		// Execute query
		ResultSet ret = MySQLConnection.gI().query(query.toString());

		// Create list of result
		List<E> list = new ArrayList<E>();

		// Move next
		while (ret.next()) {
			final E newE = aClass.newInstance();

			// foreach element in map
			columnIterator = mapOfColumn.keySet().iterator();
			while (columnIterator.hasNext()) {
				// Get k, v
				String k = columnIterator.next();
				Field v  = mapOfColumn.get(k);

				// if field can't access then set access
				if (Modifier.isPrivate(v.getModifiers()))
					v.setAccessible(true);

				// type of string
				if (v.getType() == String.class)
					v.set(newE, ret.getString(k));
				// type of boolean
				else if (v.getType() == boolean.class || v.getType() == Boolean.class)
					v.set(newE, ret.getBoolean(k));
				// type byte
				else if (v.getType() == byte.class || v.getType() == Byte.class)
					v.set(newE, ret.getByte(k));
				// type short
				else if (v.getType() == short.class || v.getType() == Short.class)
					v.set(newE, ret.getShort(k));
				// type int
				else if (v.getType() == int.class || v.getType() == Integer.class)
					v.set(newE, ret.getInt(k));
				// type long
				else if (v.getType() == long.class || v.getType() == Long.class)
					v.set(newE, ret.getLong(k));
				// Can't recognize type
				else
					throw new LoaderException("Can't recognize type of field: "+v.getName());
			}

			list.add(newE);
		}

		return list;
	}

	/**
	 * Use eager loading to load list entity from db to memory<br/>
	 * When use this, onetomany relative will be loader
	 * @param aClass Define entity class to be loaded
	 * @return       List of entity in database
	 * @throws LoaderException if there are some error while loading
	 * @throws SQLException    if there are some error while execute sql
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static final <E> List<E> eagerLoading(Class<E> aClass, boolean printQuery) throws LoaderException, SQLException, InstantiationException, IllegalAccessException {
		int i, j, tabId;

		Table tab; Column col; OneToMany otm; JoinColumn jc;

		TableRef t, t2, tabMain; ColumnRef c, c2;

		String tabName, tabName2, tabSelectName, colName;

		Class<?> bClass, cClass;

		// Create list of class and table
		List<TableRef> listOfTable = new ArrayList<TableRef>();

		/**
		 * Algothirm:
		 * 	+ first add first table to queue
		 *  + while queue not empty
		 *  	+ get table from queue, add field
		 *  	+ if table relate to other table add it to queue
		 */

		// Create queue and add aClass to queue
		Queue<Class<?>> queueOfClass;
		(queueOfClass = new PriorityQueue<Class<?>>()).offer(aClass);

		// Init tabID
		tabId = 0;

		// Tab main
		tabMain = null;

		// Do algothirm until queue empty
		while (!queueOfClass.isEmpty()) {
			bClass = queueOfClass.poll();

//			System.out.println("Class "+bClass);

			// Determine table name
			tabName = (tab = bClass.getAnnotation(Table.class)) == null ? bClass.getName() : tab.name();

			// Create tab select name
			tabSelectName = "tab"+(tabId++);

			// Create table object
			t = new TableRef(tabSelectName, tabName, bClass);

			Field[] fields = bClass.getDeclaredFields();
			for (i = 0; i < fields.length; i++) {
				Field f = fields[i];

//				System.out.println("Field "+f);

				// If is a column
				if ((col = f.getAnnotation(Column.class)) != null) {
					// Create column
					c = new ColumnRef(t, col.name(), null, f);

					// Check id key
					if (f.getAnnotation(Id.class) != null)
						t.setIdKey(c);

					// Add to table
					t.addColumn(c);
				}

				// If is a relative
				if ((otm = f.getAnnotation(OneToMany.class)) != null) {
					cClass = null;

					// if it is not a list
					if (!List.class.isAssignableFrom(f.getType()))
						throw new LoaderException("Field "+f.getName()+": OneToMany relate must be list to process!");
					
					// Get class in list
					cClass = (Class<?>) ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];

					// Check tabname
					tabName2 = (tab = cClass.getAnnotation(Table.class)) == null ? cClass.getName() : tab.name();

					if (!tabName2.equalsIgnoreCase(otm.mappedBy()))
						throw new LoaderException("Field "+f.getName()+": Entity specific by OneToMany not have the same table name in mappedBy!");

					// create column
					c = new ColumnRef(t, null, otm.mappedBy(), f);

					// Add to table
					t.addColumn(c);
					
					// add to queue
					queueOfClass.offer(cClass);
				}

				// if is a many to one
				if (f.getAnnotation(ManyToOne.class) != null) {
					// get relative
					t2 = null; cClass = f.getType();
					for (j = 0; j < listOfTable.size(); j++) {
						if (listOfTable.get(j).getClassRef() == cClass)
							t2 = listOfTable.get(j);
					}

					// check table
					if (t2 == null)
						throw new LoaderException("Field "+f.getName()+": Entity relative not found!");

					// check private key
					if (t2.getIdKey() == null)
						throw new LoaderException("Field "+f.getName()+": Entity relative primary key missing!");

					// if no join column
					if ((jc = f.getAnnotation(JoinColumn.class)) == null)
						colName = t2.getIdKey().getName();
					else
						colName = jc.name();

					// check mappedBy
					if ((c2 = t2.getRelativeColumn(t.getName())) == null) 
						throw new LoaderException("Field "+f.getName()+": Entity relative mapping list missing!");

					// Set mappedBy table
					c2.setMappedTab(t);

					// Create column
					c = new ColumnRef(t, colName, null, f);

					// Add relative to primary key of t2
					c.setRelative(c2);

					// Add to table
					t.addColumn(c);
				}
			}

			// Set tab main
			if (tabMain == null)
				tabMain = t;

			// Add tab
			listOfTable.add(t);
		}

		// Create query
		StringBuilder query = new StringBuilder("SELECT ");
		StringBuilder from  = new StringBuilder(" FROM ");
		StringBuilder where = new StringBuilder("");

		// Put to query
		Iterator<TableRef> tabIterator = listOfTable.iterator();

		// first column
		boolean first = true;

		// first from
		boolean firstFrom = true;

		// first where
		boolean firstWhere = true;

		while (tabIterator.hasNext()) {
			// Next table
			t = tabIterator.next();

			// add from
			if (firstFrom) {
				from.append(t.getName()+" "+t.getSelectName());
				firstFrom = false;
			} else
				from.append(", "+t.getName()+" "+t.getSelectName());

			// get the column iterator
			Iterator<ColumnRef> columnIterator = t.getColumnIterator();
			while (columnIterator.hasNext()) {
				// Next column
				c = columnIterator.next();

				// If many to one
				if ((c2 = c.getRelative()) != null) {
					// Mark hasWhere
					if (firstWhere) {
						firstWhere = false;
						where.append(" WHERE "+c.getSelectName()+"="+c2.getTable().getIdKey().getSelectName());
					} else
						where.append(","+c.getSelectName()+"="+c2.getTable().getIdKey().getSelectName());
				}
				// if many to one
				else if (c.getName() == null) {
					// do nothing
				}
				// Put to query
				else if (first) {
					first = false;
					query.append(c.getSelectName());
				} else
					query.append(", ").append(c.getSelectName());
			}
		}

		String queryStr = query.toString()+from.toString()+where.toString();

		// print query
		if (printQuery)
			System.out.println("Q: "+queryStr);

		// Execute query
		ResultSet ret = MySQLConnection.gI().query(queryStr);

		// Create list of result
		List<E> list = new ArrayList<E>();

		// Move next
		while (ret.next()) {
			tabMain.getObject(ret, list, null);
		}

		return list;
	}

}
