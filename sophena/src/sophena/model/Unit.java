package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_units")
public class Unit extends RootEntity {

	@Column(name = "quantity")
	@Enumerated(EnumType.STRING)
	private Quantity quantity;

	@Column(name = "is_reference_unit")
	private boolean referenceUnit;

	@Column(name = "conversion_factor")
	private double conversionFactor;

	public Quantity getQuantity() {
		return quantity;
	}

	public void setQuantity(Quantity quantity) {
		this.quantity = quantity;
	}

	public void setReferenceUnit(boolean referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

	public boolean isReferenceUnit() {
		return referenceUnit;
	}

	public double getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}
}
