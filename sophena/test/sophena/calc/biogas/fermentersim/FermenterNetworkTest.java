package sophena.calc.biogas.fermentersim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Fermenter;

public class FermenterNetworkTest {

	@Test
	public void testSingleFermenter() {
		var plant = new BiogasPlant();
		var f = fermenter("F", 38, 10);
		plant.fermenters.add(f);

		var nodes = FermenterNetwork.of(plant).orElseThrow().nodes();
		assertEquals(1, nodes.size());
		var node = nodes.getFirst();
		assertSame(f, node.fermenter());
		assertEquals(1.0, node.massShare(), 1e-12);
		assertEquals(1.0, node.gasShare(), 1e-12);
		assertTrue(node.isInput());
		assertNull(node.feedTemperature());
	}

	@Test
	public void testParallelFermentersShareTheInputByVolume() {
		var plant = new BiogasPlant();
		var a = fermenter("A", 38, 10);
		var b = fermenter("B", 38, 30);
		plant.fermenters.add(a);
		plant.fermenters.add(b);

		var network = FermenterNetwork.of(plant).orElseThrow();
		assertEquals(0.25, nodeOf(network, a).massShare(), 1e-12);
		assertEquals(0.75, nodeOf(network, b).massShare(), 1e-12);
		assertEquals(0.25, nodeOf(network, a).gasShare(), 1e-12);
		assertEquals(0.75, nodeOf(network, b).gasShare(), 1e-12);
	}

	@Test
	public void testSerialFermentersGetTheFullMass() {
		var plant = new BiogasPlant();
		var b = fermenter("B", 40, 10);
		var a = fermenter("A", 38, 10);
		a.successorId = b.id;
		// the order in the list should not matter
		plant.fermenters.add(b);
		plant.fermenters.add(a);

		var nodes = FermenterNetwork.of(plant).orElseThrow().nodes();
		assertSame(a, nodes.get(0).fermenter());
		assertSame(b, nodes.get(1).fermenter());
		assertEquals(1.0, nodes.get(0).massShare(), 1e-12);
		assertEquals(1.0, nodes.get(1).massShare(), 1e-12);
		assertEquals(0.5, nodes.get(1).gasShare(), 1e-12);
		assertTrue(nodes.get(0).isInput());
		assertEquals(38.0, nodes.get(1).feedTemperature(), 1e-12);
	}

	@Test
	public void testMergeHasMixedFeedTemperature() {
		var plant = new BiogasPlant();
		var a = fermenter("A", 36, 10);
		var b = fermenter("B", 40, 30);
		var c = fermenter("C", 42, 20);
		a.successorId = c.id;
		b.successorId = c.id;
		plant.fermenters.add(c);
		plant.fermenters.add(a);
		plant.fermenters.add(b);

		var network = FermenterNetwork.of(plant).orElseThrow();
		var node = nodeOf(network, c);
		assertSame(c, network.nodes().getLast().fermenter());
		assertEquals(1.0, node.massShare(), 1e-12);
		// 0.25 * 36 + 0.75 * 40 = 39
		assertEquals(39.0, node.feedTemperature(), 1e-12);
		assertEquals(20.0 / 60.0, node.gasShare(), 1e-12);
	}

	@Test
	public void testColderSuccessorIsNotHeatedByTheFeed() {
		var plant = new BiogasPlant();
		var a = fermenter("A", 42, 10);
		var b = fermenter("B", 38, 10);
		a.successorId = b.id;
		plant.fermenters.add(a);
		plant.fermenters.add(b);

		var network = FermenterNetwork.of(plant).orElseThrow();
		assertEquals(38.0, nodeOf(network, b).feedTemperature(), 1e-12);
	}

	@Test
	public void testUnknownSuccessorIsIgnored() {
		var plant = new BiogasPlant();
		var a = fermenter("A", 38, 10);
		a.successorId = "does-not-exist";
		plant.fermenters.add(a);
		var nodes = FermenterNetwork.of(plant).orElseThrow().nodes();
		assertTrue(nodes.getFirst().isInput());
	}

	@Test
	public void testCycleIsAnError() {
		var plant = new BiogasPlant();
		var a = fermenter("A", 38, 10);
		var b = fermenter("B", 38, 10);
		var c = fermenter("C", 38, 10);
		a.successorId = b.id;
		b.successorId = c.id;
		c.successorId = b.id;
		plant.fermenters.add(a);
		plant.fermenters.add(b);
		plant.fermenters.add(c);
		assertTrue(FermenterNetwork.of(plant).isError());
	}

	@Test
	public void testInvalidPlants() {
		var plant = new BiogasPlant();
		assertTrue(FermenterNetwork.of(plant).isError());

		var f = fermenter("F", 38, 10);
		f.wallTotalHeight = 0;
		plant.fermenters.add(f);
		assertTrue(FermenterNetwork.of(plant).isError());
	}

	/// Creates a fermenter with the given volume in m³.
	static Fermenter fermenter(String name, double temperature, double volume) {
		var f = Fermenter.createDefault(name);
		f.targetTemperature = temperature;
		double r = f.wallInnerRadius();
		f.wallTotalHeight = volume / (Math.PI * r * r);
		assertEquals(volume, f.volume(), 1e-9);
		return f;
	}

	private static FermenterNetwork.Node nodeOf(
		FermenterNetwork network, Fermenter f
	) {
		for (var node : network.nodes()) {
			if (node.fermenter() == f)
				return node;
		}
		throw new AssertionError("fermenter not found: " + f.name);
	}
}
