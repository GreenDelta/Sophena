package sophena.io;

import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.google.gson.JsonObject;

public class GsonTest {

	@Test
	public void testGetNullMembers() {
		JsonObject obj = new JsonObject();
		assertNull(obj.getAsJsonArray("shouldBeNull"));
		assertNull(obj.getAsJsonObject("shouldBeNull"));
		assertNull(obj.getAsJsonPrimitive("shouldBeNull"));
		assertNull(obj.remove("shouldBeNull"));
	}

	@Test(expected = IllegalStateException.class)
	public void testWrongConversions() {
		assertNull(new JsonObject().getAsJsonArray());
	}

}
