package sophena.rcp.charts;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;

import sophena.model.Stats;

public class Charts {

	private Charts() {
	}

	public static CircularBufferDataProvider dataProvider(double[] data) {
		CircularBufferDataProvider d = new CircularBufferDataProvider(true);
		d.setBufferSize(Stats.HOURS);
		d.setConcatenate_data(true);
		d.setCurrentYDataArray(data);
		return d;
	}

	public static CircularBufferDataProvider dataProvider() {
		CircularBufferDataProvider b = new CircularBufferDataProvider(true);
		b.setBufferSize(Stats.HOURS);
		b.setConcatenate_data(false);
		return b;
	}

}
