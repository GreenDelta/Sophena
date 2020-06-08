package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_manufacturer")
public class Manufacturer extends BaseDataEntity {

	@Column(name = "address")
	public String address;

	@Column(name = "url")
	public String url;

	@Column(name = "logo")
	public String logo;
}
