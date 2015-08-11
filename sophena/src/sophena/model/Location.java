package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_locations")
public class Location extends AbstractEntity {

	@Column(name = "name")
	public String name;

	@Column(name = "street")
	public String street;

	@Column(name = "zip_code")
	public String zipCode;

	@Column(name = "city")
	public String city;

	@Column(name = "latitude")
	public double latitude;

	@Column(name = "longitude")
	public double longitude;

	@Override
	public Location clone() {
		Location clone = new Location();
		clone.setId(UUID.randomUUID().toString());
		clone.name = name;
		clone.street = street;
		clone.zipCode = zipCode;
		clone.city = city;
		clone.latitude = latitude;
		clone.longitude = longitude;
		return clone;
	}
}
