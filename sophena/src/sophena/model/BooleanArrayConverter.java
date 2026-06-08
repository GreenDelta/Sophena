package sophena.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BooleanArrayConverter implements AttributeConverter<boolean[], byte[]> {

	@Override
	public byte[] convertToDatabaseColumn(boolean[] booleans) {
		if (booleans == null)
			return null;
		byte[] bytes = new byte[booleans.length];
		for (int i = 0; i < booleans.length; i++) {
			bytes[i] = (byte) (booleans[i] ? 1 : 0);
		}
		return bytes;
	}

	@Override
	public boolean[] convertToEntityAttribute(byte[] bytes) {
		if (bytes == null)
			return null;
		boolean[] booleans = new boolean[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			booleans[i] = bytes[i] != 0;
		}
		return booleans;
	}
}
