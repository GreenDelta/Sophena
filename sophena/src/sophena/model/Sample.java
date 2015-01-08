package sophena.model;

// TODO: only for dev
public class Sample {

	public static Project get() {
		Project project = new Project();
		project.setName("Test project");
		// Producer p1 = new Producer();
		// p1.setName("Producer 1");
		// project.getProducers().add(p1);
		Producer p2 = new Producer();
		p2.setName("Producer 2");
		project.getProducers().add(p2);
		return project;
	}

}
