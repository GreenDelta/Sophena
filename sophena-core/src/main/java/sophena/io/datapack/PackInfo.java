package sophena.io.datapack;

public class PackInfo {

	public int version;

	static PackInfo current() {
		PackInfo info = new PackInfo();
		info.version = DataPack.VERSION;
		return info;
	}

	static PackInfo v1() {
		PackInfo info = new PackInfo();
		info.version = 1;
		return info;
	}
}
