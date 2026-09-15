package sophena.calc.biogas;

/// The algorithms that can calculate the run hours of a biogas plant.
///
/// A calculation with a different algorithm produces different run hours and
/// therefore different results of the whole project calculation, because the
/// run hours are used to create the producer profile of the plant
/// (`BiogasPlants.syncProducerProfile`).
public enum BiogasAlgorithm {

	/// The hour based algorithm: the hours of every day in which the storage of
	/// the plant could be emptied are marked by price and the plant runs in the
	/// marked hours. It does not always keep the minimum runtime of the plant,
	/// see `sophena.calc.biogas.ehours.EhourSearch`.
	HOURS("Stundenweise Preisoptimierung"),

	/// The block based algorithm: the plant runs in blocks of at least the
	/// minimum runtime and the blocks are selected by the price of the whole
	/// block, see `sophena.calc.biogas.eblocks.EblockSearch`.
	BLOCKS("Blockweise Preisoptimierung");

	/// The algorithm that is used when no other algorithm is given.
	public static final BiogasAlgorithm DEFAULT = HOURS;

	private final String label;

	BiogasAlgorithm(String label) {
		this.label = label;
	}

	/// The name of the algorithm that is shown in the user interface.
	public String label() {
		return label;
	}
}
