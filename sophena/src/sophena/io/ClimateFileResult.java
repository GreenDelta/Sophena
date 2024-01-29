package sophena.io;

public class ClimateFileResult {

	private double[] data;
	private double[] directRadiation;
	private double[] diffuseRadiation;
	private int[] itemsPerDatum;
	private boolean withoutError;
	private boolean complete;

	public double[] getData() {
		return data;
	}

	void setData(double[] data) {
		this.data = data;
	}

	public double[] getDirectRadiation() {
		return directRadiation;
	}

	void setDirectRadiation(double[] directRadiation) {
		this.directRadiation = directRadiation;
	}

	public double[] getDiffuseRadiation() {
		return diffuseRadiation;
	}

	void setDiffuseRadiation(double[] diffuseRadiation) {
		this.diffuseRadiation = diffuseRadiation;
	}

	public int[] getItemsPerDatum() {
		return itemsPerDatum;
	}

	void setItemsPerDatum(int[] itemsPerDatum) {
		this.itemsPerDatum = itemsPerDatum;
	}

	public boolean isWithoutError() {
		return withoutError;
	}

	void setWithoutError(boolean withoutError) {
		this.withoutError = withoutError;
	}

	public boolean isComplete() {
		return complete;
	}

	void setComplete(boolean complete) {
		this.complete = complete;
	}
}
