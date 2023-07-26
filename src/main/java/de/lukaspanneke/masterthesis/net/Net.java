package de.lukaspanneke.masterthesis.net;

import de.lukaspanneke.masterthesis.components.Place;
import de.lukaspanneke.masterthesis.components.Transition;
import de.lukaspanneke.masterthesis.logic.Formula;
import de.lukaspanneke.masterthesis.logic.Variable;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record Net(Marking initialMarking) {
	public record Nodes(Set<Place> places, Set<Transition> transitions) {
	}

	public Nodes collectNodes() {
		Set<Place> places = new HashSet<>();
		Set<Transition> transitions = new HashSet<>();
		for (Place place : initialMarking.tokens().keySet()) {
			collectNodes(place, places, transitions);
		}
		return new Nodes(places, transitions);
	}

	private void collectNodes(Transition transition, Set<Place> places, Set<Transition> transitions) {
		if (transitions.add(transition)) {
			for (Place place : transition.postSet().keySet()) {
				collectNodes(place, places, transitions);
			}
		}
	}

	private void collectNodes(Place place, Set<Place> places, Set<Transition> transitions) {
		if (places.add(place)) {
			for (Transition transition : place.postSet()) {
				collectNodes(transition, places, transitions);
			}
		}
	}

	public void renderDot(Writer writer) throws IOException {
		writer.append("digraph ").append("net").append(" {\n");

		Nodes nodes = collectNodes();
		Set<Place> places = nodes.places();
		Set<Transition> transitions = nodes.transitions();

		if (!places.isEmpty()) {
			writer.append("node[shape=circle];\n");
			for (Place place : places) {
				writer.append("\"").append(place.name()).append("\"");
				Map<String, String> options = new LinkedHashMap<>();
				options.put("xlabel", place.name());
				Integer token = initialMarking().tokens().getOrDefault(place, null);
				if (token != null) {
					options.put("label", String.valueOf(token));
				} else {
					options.put("label", "");
				}
				writer.append(options.entrySet().stream()
						.map(option -> option.getKey() + "=\"" + option.getValue() + "\"")
						.collect(Collectors.joining(",", "[", "]")));
				writer.append(";\n");
			}
		}
		if (!transitions.isEmpty()) {
			writer.append("node[shape=box];\n");
			for (Transition transition : transitions) {
				writer.append("\"").append(transition.name()).append("\"");
				if (!Formula.top().equals(transition.guard())) {
					writer.append("[xlabel=\"").append(transition.guard().toString()).append("\"]");
				}
				writer.append(";\n");
			}
		}

		for (Transition transition : transitions) {
			for (Map.Entry<Place, Variable> entry : transition.preSet().entrySet()) {
				Place place = entry.getKey();
				Variable variable = entry.getValue();
				writer.append("\"").append(place.name()).append("\" -> \"").append(transition.name()).append("\"")
						.append("[xlabel=\"").append(variable.name()).append("\"]").append("\n");
			}
			for (Map.Entry<Place, Variable> entry : transition.postSet().entrySet()) {
				Place place = entry.getKey();
				Variable variable = entry.getValue();
				writer.append("\"").append(transition.name()).append("\" -> \"").append(place.name()).append("\"")
						.append("[xlabel=\"").append(variable.name()).append("\"]").append("\n");
			}
		}

		writer.append("}\n");
	}

	public void renderLlPep(Writer writer) throws IOException {
		writer.append("PEP\n");
		writer.append("PTNet\n");
		writer.append("FORMAT_N\n");

		writer.append("PL\n");
		Nodes nodes = collectNodes();
		for (Place place : nodes.places()) {
			writer
					.append(String.valueOf(place.index()))
					.append("\"").append(place.name()).append("\"")
					.append("0@0M").append(String.valueOf(initialMarking().tokens().getOrDefault(place, 0)))
					.append("\n");
		}

		writer.append("TR\n");
		for (Transition transition : nodes.transitions()) {
			writer
					.append(String.valueOf(transition.index()))
					.append("\"").append(transition.name()).append("\"")
					.append("0@0")
					.append("\n");
		}

		writer.append("TP\n");
		for (Transition transition : nodes.transitions()) {
			for (Place place : transition.postSet().keySet()) {
				writer
						.append(String.valueOf(transition.index()))
						.append("<")
						.append(String.valueOf(place.index()))
						.append("\n");
			}
		}

		writer.append("PT\n");
		for (Place place : nodes.places()) {
			for (Transition transition : place.postSet()) {
				writer
						.append(String.valueOf(place.index()))
						.append(">")
						.append(String.valueOf(transition.index()))
						.append("\n");
			}
		}
	}
}
