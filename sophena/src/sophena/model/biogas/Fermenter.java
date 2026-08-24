package sophena.model.biogas;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import sophena.model.AbstractEntity;

/**
 * A fermenter of a biogas plant.
 */
@Entity
@Table(name = "tbl_fermenters")
public class Fermenter extends AbstractEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "roof_type")
	public RoofType roofType;

	@Override
	public Fermenter copy() {
		var clone = new Fermenter();
		clone.id = UUID.randomUUID().toString();
		clone.roofType = roofType;
		return clone;
	}
}
