package sophena.blocks;

import java.io.File;

import sophena.calc.biogas.eblocks.BlockSearch;
import sophena.db.Database;
import sophena.model.biogas.BiogasPlant;




public class BlockAlg {

	static void main() {
		var dbDir = new File("build/test-workspace/database");
		try (var db = new Database(dbDir)) {
			var plant = db.getAll(BiogasPlant.class).getFirst();
			System.out.println(plant.name);

			new BlockSearch(plant).run();




		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
