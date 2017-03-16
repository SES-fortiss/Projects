package org.fortiss.smg.sqltools.lib.builder;

import static org.junit.Assert.*;

import org.fortiss.smg.sqltools.lib.builder.IllegalQueryException;
import org.fortiss.smg.sqltools.lib.builder.UpdateQuery;
import org.junit.Test;

public class UpdateQueryTest {

	@Test(expected = IllegalQueryException.class)
	public void shouldThrowErroWhenSetIsNotPassed() throws Exception {
		// Given
		UpdateQuery query = new UpdateQuery("persons p");

		// When
		query.toString();
	}

	@Test
	public void shouldCreateQueryForTableUsingSets() throws Exception {
		// Given
		UpdateQuery query = new UpdateQuery("persons p").set("p.name", "foo");

		// When
		String actual = query.toString();

		// Then
		String expected = "UPDATE persons p SET p.name = 'foo'";
		assertEquals(expected, actual);
	}

	@Test
	public void shouldCreateQueryUsingWhere() throws Exception {
		// Given
		UpdateQuery query = new UpdateQuery("employee e").set("e.salary", "50000")
														 .addWhere("e.age > 40")
														 .addWhere("e.genre = 'female'");

		// When
		String actual = query.toString();

		// Then
		String expected = "UPDATE employee e SET e.salary = '50000' WHERE e.age > 40 AND e.genre = 'female'";
		assertEquals(expected, actual);
	}

}