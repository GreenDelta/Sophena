package sophena.rcp.editors.producers;

import java.util.Optional;

import org.eclipse.ui.IEditorInput;

import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.descriptors.ProducerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.app.App;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.utils.Producers;

public record ProducerEditorInput(Project project, Producer producer) {

	static void open(ProjectDescriptor proj, ProducerDescriptor prod) {
		if (proj == null || prod == null) return;
		var project = App.getDb().get(Project.class, proj.id);
		var producer = Producers.findById(project, prod.id);
		if (producer == null) return;

		var cacheKey = App.stash(new ProducerEditorInput(project, producer));
		var input = new KeyEditorInput(cacheKey, producer.name);
		var editorId = producer.biogasPlant  != null
			? "sophena.BiogasPlantProducerEditor"
			: "sophena.ProducerEditor";
		Editors.open(input, editorId);
	}

	public static Optional<ProducerEditorInput> getFrom(IEditorInput input) {
		if (!(input instanceof KeyEditorInput keyInput))
			return Optional.empty();
		var stashed = App.pop(keyInput.getKey());
		return stashed instanceof ProducerEditorInput prodInput
			? Optional.of(prodInput)
			: Optional.empty();
	}
}
