package sophena.model;

import java.util.UUID;

// TODO: only for dev
public class Sample {

	public static Project get() {
		Project project = new Project();
		project.setId("project");
		project.setName("Test project");

		Producer p1 = new Producer();
		p1.setId("p1");
		p1.setX(60);
		p1.setY(60);
		p1.setName("Producer 1");
		project.getProducers().add(p1);

		Producer p2 = new Producer();
		p2.setId("p2");
		p2.setX(360);
		p2.setY(160);
		p2.setName("Producer 2");
		project.getProducers().add(p2);

		Pipe pipe = new Pipe();
		pipe.setId(UUID.randomUUID().toString());
		pipe.setProviderId(p1.getId());
		pipe.setRecipientId(p2.getId());
		project.getPipes().add(pipe);

		return project;
	}
}
