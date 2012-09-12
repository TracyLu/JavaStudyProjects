package net.madz.download.service.metadata;

import java.util.LinkedList;

public class ServiceMetadata {
	private String name;
	private String shortName;
	private String description;
	private LinkedList<Argument> arguments;
	private LinkedList<Option> options;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public LinkedList<Argument> getArguments() {
		return arguments;
	}

	public void setArguments(LinkedList<Argument> arguments) {
		this.arguments = arguments;
	}

	public LinkedList<Option> getOptions() {
		return options;
	}

	public void setOptions(LinkedList<Option> options) {
		this.options = options;
	}

}
