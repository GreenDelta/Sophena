package sophena.rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;
import sophena.db.daos.BoilerDao;
import sophena.db.daos.Dao;
import sophena.db.daos.HeatPumpDao;
import sophena.db.daos.ProductDao;
import sophena.db.daos.SolarCollectorDao;
import sophena.db.usage.UsageSearch;
import sophena.model.BufferTank;
import sophena.model.FlueGasCleaning;
import sophena.model.HeatRecovery;
import sophena.model.Pipe;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.MsgBox;

class ProductCleanup implements Runnable {

	@Override
	public void run() {
		boolean doIt = MsgBox.ask("Produktdatenbank zurücksetzen?",
				"Diese Aktion löscht alle Produkte der "
						+ "Produktdatenbank, die nicht selbst "
						+ "erfasst wurden und die nicht in einem Projekt verwendet werden. Soll dies ausgeführt werden?");
		if (!doIt)
			return;

		Editors.closeAll();
		IProgressService progress = PlatformUI.getWorkbench()
				.getProgressService();
		try {
			progress.run(true, false, monitor -> {
				monitor.beginTask("Lösche Produkte", IProgressMonitor.UNKNOWN);
				deleteProducts();
				monitor.done();
			});

		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to delete products", e);
		}
	}

	private void deleteProducts() {
		Database db = App.getDb();
		if (db == null)
			return;
		UsageSearch usage = new UsageSearch(db);

		// boilers
		BoilerDao bdao = new BoilerDao(db);
		bdao.getAll().stream().forEach(b -> {
			if (!b.isProtected)
				return;
			if (usage.of(b).isEmpty()) {
				bdao.delete(b);
			}
		});

		// generic products
		ProductDao pdao = new ProductDao(db);
		pdao.getAll().stream().forEach(p -> {
			if (!p.isProtected)
				return;
			if (usage.of(p).isEmpty()) {
				pdao.delete(p);
			}
		});

		// heat recoveries
		Dao<HeatRecovery> hrDao = new Dao<>(HeatRecovery.class, db);
		hrDao.getAll().stream().forEach(hr -> {
			if (!hr.isProtected)
				return;
			if (usage.of(hr).isEmpty()) {
				hrDao.delete(hr);
			}
		});

		// flue gas cleaning
		Dao<FlueGasCleaning> fgcDao = new Dao<>(FlueGasCleaning.class, db);
		fgcDao.getAll().stream().forEach(fgc -> {
			if (!fgc.isProtected)
				return;
			if (usage.of(fgc).isEmpty()) {
				fgcDao.delete(fgc);
			}
		});

		// buffer tanks
		Dao<BufferTank> bufDao = new Dao<>(BufferTank.class, db);
		bufDao.getAll().stream().forEach(buf -> {
			if (!buf.isProtected)
				return;
			if (usage.of(buf).isEmpty()) {
				bufDao.delete(buf);
			}
		});

		// pipes
		Dao<Pipe> pipDao = new Dao<>(Pipe.class, db);
		pipDao.getAll().stream().forEach(pip -> {
			if (!pip.isProtected)
				return;
			if (usage.of(pip).isEmpty()) {
				pipDao.delete(pip);
			}
		});

		// transfer stations
		Dao<FlueGasCleaning> tsDao = new Dao<>(FlueGasCleaning.class, db);
		tsDao.getAll().stream().forEach(ts -> {
			if (!ts.isProtected)
				return;
			if (usage.of(ts).isEmpty()) {
				tsDao.delete(ts);
			}
		});
		
		// solar collectors
		SolarCollectorDao sdao = new SolarCollectorDao(db);
		sdao.getAll().stream().forEach(b -> {
			if (!b.isProtected)
				return;
			if (usage.of(b).isEmpty()) {
				sdao.delete(b);
			}
		});
		
		// heat pumps
		HeatPumpDao hpdao = new HeatPumpDao(db);
		hpdao.getAll().stream().forEach(b -> {
			if (!b.isProtected)
				return;
			if (usage.of(b).isEmpty()) {
				hpdao.delete(b);
			}
		});
	}
}
