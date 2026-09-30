package sophena.io.thermos.pipes;

import java.util.ArrayList;
import java.util.List;

import sophena.io.thermos.ThermosImportConfig;
import sophena.model.Pipe;

/// Runs the pipe dimensioning that is used by the thermos import to check
/// whether a given product line of pipes can be used for a network.
public final class PipeDimensioning {

	private PipeDimensioning() {}

	/// Returns `null` when the given pipes can be used for the whole network of
	/// the given import configuration, otherwise the reason why the
	/// dimensioning failed.
	public static String errorOf(ThermosImportConfig config, List<Pipe> pipes) {
		if (config == null || pipes == null || pipes.isEmpty())
			return null;
		var file = config.thermosFile();
		if (file == null || file.network() == null) return null;
		try {
			// the pipe config sorts the given list in place, thus we pass a copy
			var pipeConfig = PipeConfig.of(
				config.project(), new ArrayList<>(pipes));
			var plan = PipePlan.of(pipeConfig, file.network());

			return plan.isError()
				? plan.error()
				: null;
		} catch (Exception e) {
			return "Dimensioning failed: " + e.getMessage();
		}
	}
}
