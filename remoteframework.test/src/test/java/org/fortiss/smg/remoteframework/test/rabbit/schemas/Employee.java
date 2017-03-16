package org.fortiss.smg.remoteframework.test.rabbit.schemas;

public class Employee implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1668687379276934226L;
	public String name;
	public String address;
	public int SSN;
	public int number;

	public Employee() {

	}

	public Employee(String name, String address, int sSN, int number) {
		super();
		this.name = name;
		this.address = address;
		SSN = sSN;
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getSSN() {
		return SSN;
	}

	public void setSSN(int sSN) {
		SSN = sSN;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

}