package at.ac.univie.dse2016.stream;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BoerseTest extends Boerse {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEmitentAddNew() {
		Boerse boerse = new Boerse();
		Emittent emittent = new Emittent(1, "AAPL", "Apple");
	}

	@Test
	public void testEmitentEdit() {
		fail("Not yet implemented");
	}

	@Test
	public void testEmitentLock() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetEmitentsList() {
		fail("Not yet implemented");
	}

}
