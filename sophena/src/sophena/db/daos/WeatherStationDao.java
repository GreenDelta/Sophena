package sophena.db.daos;

import java.util.ArrayList;
import java.util.List;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.WeatherStation;
import sophena.model.descriptors.WeatherStationDescriptor;

public class WeatherStationDao extends RootEntityDao<WeatherStation> {

	public WeatherStationDao(Database db) {
		super(WeatherStation.class, db);
	}

	public List<WeatherStationDescriptor> getDescriptors() {
		String sql = "SELECT id, name, description FROM tbl_weather_stations";
		List<WeatherStationDescriptor> list = new ArrayList<>();
		try {
			NativeSql.on(db).query(sql, (r) -> {
				WeatherStationDescriptor d = new WeatherStationDescriptor();
				d.setId(r.getString(1));
				d.setName(r.getString(2));
				d.setDescription(r.getString(3));
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get descriptors for " + getType(), e);
		}
		return list;
	}

}
