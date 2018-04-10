package sophena.io.datapack;

import com.google.gson.JsonObject;

import sophena.model.ModelType;

interface Upgrade {

	void on(ModelType type, JsonObject obj);

}
