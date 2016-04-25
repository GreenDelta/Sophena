package sophena.db.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.BuildingState;
import sophena.model.Fuel;
import sophena.model.ModelType;
import sophena.model.Pipe;
import sophena.model.Product;
import sophena.model.TransferStation;

/**
 * Searches for the usage of entities in other entities.
 */
public class UsageSearch {

	private Database database;

	public UsageSearch(Database database) {
		this.database = database;
	}

	public List<SearchResult> of(Boiler boiler) {
		if (boiler == null || boiler.id == null)
			return Collections.emptyList();
		String sql = "select p.id, p.name from tbl_producers p "
				+ "where f_boiler = '" + boiler.id + "'";
		return query(sql, ModelType.PRODUCER);
	}

	public List<SearchResult> of(BufferTank tank) {
		if (tank == null || tank.id == null)
			return Collections.emptyList();
		String sql = "select p.id, p.name from tbl_projects p inner join "
				+ "tbl_heat_nets h on p.f_heat_net = h.id where "
				+ "h.f_buffer_tank = '" + tank.id + "'";
		return query(sql, ModelType.PROJECT);
	}

	public List<SearchResult> of(Product product) {
		if (product == null || product.id == null)
			return Collections.emptyList();
		String sql = "select p.id, p.name from tbl_product_entries e inner join "
				+ "tbl_projects p on e.f_project = p.id where e.f_product = '"
				+ product.id + "'";
		return query(sql, ModelType.PROJECT);
	}

	public List<SearchResult> of(Pipe pipe) {
		if (pipe == null || pipe.id == null)
			return Collections.emptyList();
		String sql = "select distinct p.id, p.name from tbl_heat_net_pipes hp "
				+ "inner join tbl_heat_nets hn on hp.f_project = hn.id "
				+ "inner join tbl_projects p on p.f_heat_net = hn.id "
				+ " where hp.f_pipe = '" + pipe.id + "'";
		return query(sql, ModelType.PROJECT);
	}

	public List<SearchResult> of(BuildingState state) {
		if (state == null || state.id == null)
			return Collections.emptyList();
		String sql = "select c.id, c.name from tbl_consumers c where "
				+ "c.f_building_state = '" + state.id + "'";
		return query(sql, ModelType.CONSUMER);
	}

	public List<SearchResult> of(TransferStation station) {
		if (station == null || station.id == null)
			return Collections.emptyList();
		String sql = "select c.id, c.name from tbl_consumers c where "
				+ "c.f_transfer_station = '" + station.id + "'";
		return query(sql, ModelType.CONSUMER);
	}
	
	public List<SearchResult> of(FlueGasCleaning cleaning) {
		if (cleaning == null || cleaning.id == null)
			return Collections.emptyList();
		String sql = "select p.id, p.name from tbl_flue_gas_cleaning_entries e "
				+ " inner join tbl_projects p on e.f_project = p.id "
				+ " where e.f_flue_gas_cleaning = '" + cleaning.id + "'";
		return query(sql, ModelType.PROJECT);
	}

	public List<SearchResult> of(HeatRecovery recovery) {
		if (recovery == null || recovery.id == null)
			return Collections.emptyList();
		String sql = "select p.id, p.name from tbl_producers p "
				+ "where p.f_heat_recovery = '" + recovery.id + "'";
		return query(sql, ModelType.PRODUCER); 
	}

	public List<SearchResult> of(Fuel fuel) {
		if (fuel == null || fuel.id == null)
			return Collections.emptyList();
		String boilerSql = "select b.id, b.name from tbl_boilers b where "
				+ "b.f_fuel = '" + fuel.id + "'";
		List<SearchResult> boilerList = query(boilerSql, ModelType.BOILER);
		String consumerSql = "select c.id, c.name from tbl_consumers c inner "
				+ " join tbl_fuel_consumptions fc on fc.f_consumer = c.id where "
				+ " fc.f_fuel = '" + fuel.id + "'";
		List<SearchResult> consumerList = query(consumerSql, ModelType.CONSUMER);
		String producerSql = "select p.id, p.name from tbl_producers p where "
				+ "p.f_wood_fuel = '" + fuel.id + "'";
		List<SearchResult> producerList = query(producerSql, ModelType.PRODUCER);
		List<SearchResult> all = new ArrayList<>();
		all.addAll(boilerList);
		all.addAll(consumerList);
		all.addAll(producerList);
		return all;
	}

	private List<SearchResult> query(String sql, ModelType type) {
		try {
			List<SearchResult> results = new ArrayList<>();
			NativeSql.on(database).query(sql, record -> {
				SearchResult r = new SearchResult(
						record.getString(1),
						record.getString(2),
						type);
				results.add(r);
				return true;
			});
			return results;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to execute query: " + sql, e);
			return Collections.emptyList();
		}
	}
}
