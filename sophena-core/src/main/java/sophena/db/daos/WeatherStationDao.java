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
		String sql = "SELECT id, name, description, longitude, latitude, "
				+ "altitude FROM tbl_weather_stations";
		List<WeatherStationDescriptor> list = new ArrayList<>();
		try {
			NativeSql.on(db).query(sql, (r) -> {
				WeatherStationDescriptor d = new WeatherStationDescriptor();
				d.id = r.getString(1);
				d.name = r.getString(2);
				d.description = r.getString(3);
				d.longitude = r.getDouble(4);
				d.latitude = r.getDouble(5);
				d.altitude = r.getDouble(6);
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get descriptors for " + getType(), e);
		}
		return list;
	}

}
