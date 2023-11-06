package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
	public TimeInterval copy() {
		var clone = new TimeInterval();
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
