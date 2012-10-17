package org.lds.rs.dto;



public class ListObject {
	public Object id;
	public Object name;


	public ListObject(Object id, Object name) {
		this.id = id;
		this.name = name;
	}

	// Getters and Setters ----------------------------------------------------->


	/**
	 * @return the id
	 */
	public Object getId() {
		return id;
	}
	/**
	 * @return the name
	 */
	public Object getName() {
		return name;
	}

}
