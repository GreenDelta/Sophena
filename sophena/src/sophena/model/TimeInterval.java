package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_time_intervals")
public class TimeInterval extends AbstractEntity {

	@Column(name = "start_time")
	public String start;

	@Column(name = "end_time")
	public String end;

	@Column(name = "description")
	public String description;

	@Override
	public TimeInterval clone() {
		TimeInterval clone = new TimeInterval();
		clone.id = UUID.randomUUID().toString();
		clone.start = start;
		clone.end = end;
		clone.description = description;
		return clone;
	}

	@Override
	public String toString() {
		return "TimeInterval { start: " + start + ", end: " + end + "}";
	}

}
