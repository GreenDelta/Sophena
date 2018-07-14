package sophena.io;

public class ClimateFileResult {

	private double[] data;
	private int[] itemsPerDatum;
	private boolean withoutError;
	private boolean complete;

	public double[] getData() {
		return data;
	}

	void setData(double[] data) {
		this.data = data;
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
