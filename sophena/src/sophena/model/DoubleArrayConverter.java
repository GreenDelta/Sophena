package sophena.model;

import java.nio.ByteBuffer;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DoubleArrayConverter implements AttributeConverter<double[], byte[]> {

	@Override
	public byte[] convertToDatabaseColumn(double[] doubles) {
		if (doubles == null)
			return null;
		var buffer = ByteBuffer.allocate(doubles.length * 8);
		for (double d : doubles) {
			buffer.putDouble(d);
		}
		return buffer.array();
	}

	@Override
	public double[] convertToEntityAttribute(byte[] bytes) {
		if (bytes == null)
			return null;
		var buffer = ByteBuffer.wrap(bytes);
		int n = bytes.length / 8;
		double[] doubles = new double[n];
		for (int i = 0; i < n; i++) {
			doubles[i] = buffer.getDouble();
		}
		return doubles;
	}
}
