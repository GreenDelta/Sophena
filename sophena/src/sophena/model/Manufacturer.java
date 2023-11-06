package sophena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tbl_manufacturer")
public class Manufacturer extends BaseDataEntity {

	@Column(name = "address")
	public String address;

	@Column(name = "url")
	public String url;

	@Column(name = "logo")
	public String logo;

	@Column(name = "sponsor_order")
	public int sponsorOrder;

	@Override
	public Manufacturer copy() {
		var copy = new Manufacturer();
		copy.id = UUID.randomUUID().toString();
		copy.name = name;
		copy.description = description;
		copy.isProtected = isProtected;
		copy.address = address;
		copy.url = url;
		copy.logo = logo;
		copy.sponsorOrder = sponsorOrder;
		return copy;
	}
}
