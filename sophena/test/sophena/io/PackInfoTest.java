package sophena.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

import sophena.io.datapack.DataPack;
import sophena.io.datapack.PackInfo;

public class PackInfoTest {

	@Test
	public void testCurrent() throws Exception {
		File file = Files.createTempFile("test_pack_", ".sophena").toFile();
		assertTrue(file.delete());
		DataPack pack = new DataPack(file);
		pack.writeInfo();
		pack.close();
		pack = new DataPack(file);
		PackInfo info = pack.readInfo();
		assertEquals(DataPack.VERSION, info.version);
		pack.close();
		assertTrue(file.delete());
	}

	@Test
	public void testFallBack() throws Exception {
		File file = Files.createTempFile("test_pack_", ".sophena").toFile();
		assertTrue(file.delete());
		DataPack pack = new DataPack(file);
		pack.close();
		pack = new DataPack(file);
		PackInfo info = pack.readInfo();
		assertEquals(1, info.version);
		pack.close();
		assertTrue(file.delete());
	}
}
