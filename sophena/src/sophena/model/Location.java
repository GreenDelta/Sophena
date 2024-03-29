package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
	public Double latitude;

	@Column(name = "longitude")
	public Double longitude;

	@Override
	public Location copy() {
		var clone = new Location();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.street = street;
		clone.zipCode = zipCode;
		clone.city = city;
		clone.latitude = latitude;
		clone.longitude = longitude;
		return clone;
	}
}
