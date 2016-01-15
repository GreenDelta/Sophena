package sophena.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Instances of this class are entities that are located in the base data. Base
 * data sets are provided by the application but can also be created and
 * modified by the user. The data that are provided by the application cannot be
 * changed by a normal user.
 */
@MappedSuperclass
public abstract class BaseDataEntity extends RootEntity {

	/**
	 * If a data set is protected it cannot be modified by a user of the
	 * application.
	 */
	@Column(name = "is_protected")
	public boolean isProtected;

}
