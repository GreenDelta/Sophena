package sophena.utils;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sophena.io.Json;

public class SocketMirror {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Map<String, Handler> handlers = new HashMap<>();
	private int port = 0;

	public SocketMirror withPort(int port) {
		this.port = port;
		return this;
	}

	public void on(String method, Handler handler) {
		if (Strings.isBlank(method) || handler == null) {
			return;
		}
		handlers.put(method, handler);
	}

	public Res<Void> start() {
		if (handlers.isEmpty()) {
			return Res.error("No handlers registered");
		}

		try (var server = AsynchronousServerSocketChannel.open()) {
			server.bind(new InetSocketAddress(port));
			log.info("start socket mirror on {}", server.getLocalAddress());

			while (true) {
				try (var channel = server.accept().get()) {
					next(channel);
				}
			}
		} catch (Exception e) {
			return Res.error("Server error: " + e.getMessage());
		}
	}

	private void next(AsynchronousSocketChannel chan) {
		var json = readJsonFrom(chan);
		Res<JsonElement> res = json.isError()
			? json.castError()
			: callHandler(json.value());

		var response = new JsonObject();
		if (res.isError()) {
			response.addProperty("error", res.error());
		} else {
			response.add("result", res.value());
		}

		try {
			var bytes = new Gson()
				.toJson(response)
				.getBytes(StandardCharsets.UTF_8);
			var buffer = ByteBuffer.wrap(bytes);
			chan.write(buffer).get();
		} catch (Exception e) {
			log.error("Failed to write response to channel", e);
		}
	}

	private Res<String> readJsonFrom(AsynchronousSocketChannel chan) {
		var buf = ByteBuffer.allocate(1024);
		try (var out = new ByteArrayOutputStream()) {
			while (true) {
				buf.clear();
				int n = chan.read(buf).get();
				if (n < 0) break;
				out.write(buf.array(), 0, n);
			}
			return Res.ok(out.toString(StandardCharsets.UTF_8));
		} catch (Exception e) {
			return Res.error("Failed to read request from channel", e);
		}
	}

	private Res<JsonElement> callHandler(String json) {
		try {
			var req = JsonParser.parseString(json);
			if (!req.isJsonObject()) {
				return Res.error("Invalid request, not a JSON object");
			}

			var reqObj = req.getAsJsonObject();
			var method = Json.getString(reqObj, "method");
			if (Strings.isBlank(method)) {
				return Res.error("Invalid request, no method given");
			}

			var handler = handlers.get(method);
			if (handler == null) {
				return Res.error("No handler registered for method: " + method);
			}

			var params = reqObj.get("params");
			if (params == null) {
				params = JsonNull.INSTANCE;
			}
			return handler.exec(params);

		} catch (Exception e) {
			return Res.error("Failed to process request object", e);
		}
	}

	public interface Handler {
		Res<JsonElement> exec(JsonElement parameters);
	}

	public static void main(String[] args) {
		var a = new InetSocketAddress(0);
		System.out.println(a.getPort());
	}

}
