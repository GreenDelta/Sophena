package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * This class represents the concrete use of a pipe in a heat net of a project.
 */
@Entity
@Table(name = "tbl_heat_net_pipes")
public class HeatNetPipe extends AbstractEntity {

	@OneToOne
	@JoinColumn(name = "f_pipe")
	public Pipe pipe;

	@Embedded
	public ProductCosts costs;

	@Column(name = "name")
	public String name;
	
	@Column(name = "length")
	public double length;

	@Column(name = "price_per_meter")
	public double pricePerMeter;

	@Override
	public HeatNetPipe clone() {
		HeatNetPipe clone = new HeatNetPipe();
		clone.id = UUID.randomUUID().toString();
		clone.pipe = pipe;
		if (costs != null)
			clone.costs = costs.clone();
		clone.name = name;
		clone.length = length;
		clone.pricePerMeter = pricePerMeter;
		return clone;
	}

}
