package sophena.db.usage;

import sophena.model.ModelType;

public class SearchResult {

	public final String id;
	public final String name;
	public final ModelType type;

	SearchResult(String id, String name, ModelType type) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

}
