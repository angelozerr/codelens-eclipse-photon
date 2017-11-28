/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Add CodeMining support in SourceViewer - Bug 527515
 */
package org.eclipse.jface.text.codemining;

import java.util.List;

/**
 * Represents a reference to a command. Provides a title which will be used to represent a command
 * in the UI and, optionally, an array of arguments which will be passed to the command handler
 * function when invoked.
 * 
 * @since 3.13.0
 */
public class Command {

	/**
	 * Title of the command, like `save`.
	 */
	private String title;

	/**
	 * The identifier of the actual command handler.
	 */
	private String command;

	/**
	 * Arguments that the command handler should be invoked with.
	 */
	private List<Object> arguments;

	public Command() {
	}

	public Command(final String title, final String command) {
		this.title= title;
		this.command= command;
	}

	public Command(final String title, final String command, final List<Object> arguments) {
		this(title, command);
		this.arguments= arguments;
	}

	/**
	 * Title of the command, like `save`.
	 * 
	 * @return the title of the command, like `save`.
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Title of the command, like `save`.
	 * 
	 * @param title the title of the command, like `save`.
	 */
	public void setTitle(final String title) {
		this.title= title;
	}

	/**
	 * The identifier of the actual command handler.
	 * 
	 * @return the identifier of the actual command handler.
	 */
	public String getCommand() {
		return this.command;
	}

	/**
	 * The identifier of the actual command handler.
	 * 
	 * @param command the identifier of the actual command handler.
	 */
	public void setCommand(final String command) {
		this.command= command;
	}

	/**
	 * Arguments that the command handler should be invoked with.
	 * 
	 * @return Arguments that the command handler should be invoked with.
	 */
	public List<Object> getArguments() {
		return this.arguments;
	}

	/**
	 * Arguments that the command handler should be invoked with.
	 * 
	 * @param arguments Arguments that the command handler should be invoked with.
	 */
	public void setArguments(final List<Object> arguments) {
		this.arguments= arguments;
	}

	@Override
	public String toString() {
		StringBuilder b= new StringBuilder();
		b.append("title").append(this.title); //$NON-NLS-1$
		b.append("command").append(this.command); //$NON-NLS-1$
		b.append("arguments").append(this.arguments); //$NON-NLS-1$
		return b.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Command other= (Command) obj;
		if (this.title == null) {
			if (other.title != null)
				return false;
		} else if (!this.title.equals(other.title))
			return false;
		if (this.command == null) {
			if (other.command != null)
				return false;
		} else if (!this.command.equals(other.command))
			return false;
		if (this.arguments == null) {
			if (other.arguments != null)
				return false;
		} else if (!this.arguments.equals(other.arguments))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime= 31;
		int result= 1;
		result= prime * result + ((this.title == null) ? 0 : this.title.hashCode());
		result= prime * result + ((this.command == null) ? 0 : this.command.hashCode());
		result= prime * result + ((this.arguments == null) ? 0 : this.arguments.hashCode());
		return result;
	}
}
