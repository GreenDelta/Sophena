package sophena.utils;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
	private volatile boolean running;
	private ServerSocket server;

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
		if (running) {
			return Res.ok();
		}
		try {
			server = new ServerSocket();
			server.bind(new InetSocketAddress(port));
			running = true;
			var thread = new Thread(this::loop, "SocketMirror");
			thread.setDaemon(true);
			thread.start();
			log.info("start socket mirror on {}", server.getLocalSocketAddress());
			return Res.ok();
		} catch (Exception e) {
			running = false;
			return Res.error("Failed to start server: " + e.getMessage());
		}
	}

	private void loop() {
		while (running) {
			try (var socket = server.accept()) {
				handleRequest(socket);
			} catch (Exception e) {
				log.error("Failed to accept connection", e);
			}
		}
	}

	public void stop() {
		running = false;
		try {
			if (server != null) {
				server.close();
			}
		} catch (Exception e) {
			log.error("Failed to close server", e);
		}
	}

	private void handleRequest(Socket socket) {
		var json = readJsonFrom(socket);
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
			var out = socket.getOutputStream();
			var string = new Gson().toJson(response) + "\n";
			var bytes = string.getBytes(StandardCharsets.UTF_8);
			out.write(bytes);
			out.flush();
		} catch (Exception e) {
			log.error("Failed to write response to socket", e);
		}
	}

	private Res<String> readJsonFrom(Socket socket) {
		byte[] buf = new byte[1024];
		try (var out = new ByteArrayOutputStream()) {
			var in = socket.getInputStream();
			while (true) {
				int n = in.read(buf);
				if (n < 0) break;

				for (int i = 0; i < n; i++) {
					byte b = buf[i];
					if (b == '\n') {
						return Res.ok(out.toString(StandardCharsets.UTF_8));
					}
					if (b != '\r') {
						out.write(b);
					}
				}
			}
			return Res.ok(out.toString(StandardCharsets.UTF_8));
		} catch (Exception e) {
			return Res.error("Failed to read request from socket", e);
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
}
