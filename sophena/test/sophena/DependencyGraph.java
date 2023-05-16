package sophena;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import jakarta.persistence.Embeddable;

import sophena.model.AbstractEntity;
import sophena.model.RootEntity;

/**
 * Calculates and prints a dependency graph of the Sophena core model. The graph
 * is written in the Graphviz syntax and can be visualized e.g. via
 * http://webgraphviz.com/.
 */
public class DependencyGraph {

	public static void print() throws Exception {
		Queue<Class<?>> queue = new ArrayDeque<>();
		queue.addAll(Tests.getSubTypes(AbstractEntity.class, "sophena.model"));
		Set<Class<?>> nodes = new HashSet<>();
		List<Link> links = new ArrayList<>();
		while (!queue.isEmpty()) {
			Class<?> next = queue.poll();
			nodes.add(next);
			for (Link link : getLinks(next)) {
				Class<?> ref = link.to;
				if (!nodes.contains(ref) && !queue.contains(ref))
					queue.add(ref);
				links.add(link);
			}
		}
		printGraph(nodes, links);
	}

	private static List<Link> getLinks(Class<?> next) throws Exception {
		List<Link> links = new ArrayList<>();
		for (Field field : getFields(next)) {
			Class<?> refType = field.getType();
			if (isPrimitive(refType))
				continue;
			Link link = new Link();
			link.from = next;
			link.name = field.getName();
			if (List.class.isAssignableFrom(refType)) {
				ParameterizedType pType = (ParameterizedType) field.getGenericType();
				Class<?> type = (Class<?>) pType.getActualTypeArguments()[0];
				link.to = type;
				link.name += " (*)";
			} else {
				link.to = refType;
				link.name += " (0,1)";
			}
			links.add(link);
		}
		return links;
	}

	private static List<Field> getFields(Class<?> type) {
		List<Field> fields = new ArrayList<>();
		if (!type.getSuperclass().equals(Object.class))
			fields.addAll(getFields(type.getSuperclass()));
		for (Field field : type.getDeclaredFields()) {
			fields.add(field);
		}
		return fields;
	}

	private static boolean isPrimitive(Class<?> type) {
		if (type.isEnum())
			return true;
		Class<?>[] primitives = {
				int.class, double.class, boolean.class, double[].class,
				Double.class, String.class
		};
		for (Class<?> primitive : primitives) {
			if (type.equals(primitive))
				return true;
		}
		return false;
	}

	private static void printGraph(Set<Class<?>> nodes, List<Link> links) {
		p("digraph model {");
		p("  node [style = filled];");
		for (Class<?> node : nodes) {
			printNode(node);
		}
		p("");
		for (Link link : links) {
			p("  " + link.from.getSimpleName()
					+ " -> " + link.to.getSimpleName()
					+ " [ label = \"" + link.name + "\", fontsize = 8 ] ;");
		}
		p("}");
	}

	private static void printNode(Class<?> node) {
		String color = "white";
		if (node.isAnnotationPresent(Embeddable.class))
			color = "grey";
		else if (RootEntity.class.isAssignableFrom(node))
			color = "plum";
		else if (AbstractEntity.class.isAssignableFrom(node))
			color = "wheat";
		p("  " + node.getSimpleName() + " [color=" + color + "];");
	}

	private static void p(String s) {
		System.out.println(s);
	}

	private static class Link {
		Class<?> from;
		Class<?> to;
		String name;
	}

	public static void main(String[] args) {
		try {
			DependencyGraph.print();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
