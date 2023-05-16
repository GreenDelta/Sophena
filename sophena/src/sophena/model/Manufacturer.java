package sophena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
}
