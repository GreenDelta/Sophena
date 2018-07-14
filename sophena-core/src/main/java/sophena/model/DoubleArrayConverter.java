package sophena.model;

import java.nio.ByteBuffer;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

public class DoubleArrayConverter implements Converter {

	private static final long serialVersionUID = -5046729949761054727L;

	@Override
	public Object convertDataValueToObjectValue(Object byteData, Session session) {
		if (!(byteData instanceof byte[]))
			return null;
		byte[] bytes = (byte[]) byteData;
		double[] doubles = new double[bytes.length / 8];
		for (int i = 0; i < doubles.length; i++) {
			double d = ByteBuffer.wrap(bytes, i * 8, 8).getDouble();
			doubles[i] = d;
		}
		return doubles;
	}

	@Override
	public Object convertObjectValueToDataValue(Object doubleData,
			Session session) {
		if (!(doubleData instanceof double[]))
			return null;
		double[] doubles = (double[]) doubleData;
		byte[] bytes = new byte[doubles.length * 8];
		for (int i = 0; i < doubles.length; i++) {
			ByteBuffer.wrap(bytes, i * 8, 8).putDouble(doubles[i]);
		}
		return bytes;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {
		DatabaseField field = mapping.getField();
		field.setSqlType(java.sql.Types.BLOB);
	}

	@Override
	public boolean isMutable() {
		return false;
	}

}
