package com.epam.dlab.backendapi.core.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.epam.dlab.exceptions.DlabException;
import com.google.common.base.MoreObjects;

/** Parse command for emulate commands of Docker.
 *
 */
public class CommandParserMock {
    private String command;
    private String action;
	private String name;
	private String json;
    private Map<String, String> environment = new HashMap<String, String>();
    private Map<String, String> variable = new HashMap<String, String>();
    private List<String> others = new ArrayList<String>();

    public CommandParserMock(String command) {
		parse(command);
	}
    
    
    /** Return the name of docker command. */
    public String getCommand() {
    	return command;
    }
    
    /** Return the name of docker action. */
    public String getAction() {
    	return action;
    }
    
    /** Return name of docker container. */
    public String getName() {
    	return name;
    }
    
    /** Return content of Json if present otherwise <b>null</b>. */
    public String getJson() {
    	return json;
    }
    
    /** Return map of environment variables. */
    public Map<String, String> getEnvironment() {
    	return environment;
    }
    
    /** Return map of docker variables. */
    public Map<String, String> getVariable() {
    	return variable;
    }
    
    /** Return other single arguments. */
    public List<String> getOther() {
    	return others;
    }

    
    /** Add argument to list.
     * @param args list of arguments.
     * @param arg argument.
     */
    private void addArgToList(List<String> args, String arg) {
    	if (arg == null) {
    		return;
    	}
    	if (arg.length() > 1) {
    		if (arg.startsWith("'") && arg.endsWith("'")) {
    			arg = arg.substring(1, arg.length() - 1);
    		}
    		if (arg.startsWith("\"") && arg.endsWith("\"")) {
    			arg = arg.substring(1, arg.length() - 1);
    		}
    	}
    	arg = arg.trim();
    	if (arg.isEmpty()) {
    		return;
    	}
    	
    	args.add(arg);
    }
    
    /** Extract arguments from command line.
     * @param cmd command line.
     * @return List of arguments.
     */
    private List<String> extractArgs(String cmd) {
    	boolean isQuote = false;
    	boolean isDoubleQuote = false;
    	List<String> args = new ArrayList<String>();
    	int pos = 0;
    	
    	for (int i = 0; i < cmd.length(); i++) {
    		final char c = cmd.charAt(i);
    		if (c == '\'') {
    			isQuote = !isQuote;
    			continue;
    		}
    		if (c == '"') {
    			isDoubleQuote = !isDoubleQuote;
    			continue;
    		}
    		
    		if (!isQuote && !isDoubleQuote && c == ' ') {
    			addArgToList(args, cmd.substring(pos, i));
    			pos = i + 1;
    		}
    	}
    	if (!isQuote && !isDoubleQuote) {
    		addArgToList(args, cmd.substring(pos));
    	}
    	
    	return args;
    }
    
    /** Return the value of argument.
     * @param args list of arguments.
     * @param index index of named arguments
     * @param argName name of argument.
     */
    private String getArgValue(List<String> args, int index, String argName) {
    	if (!args.get(index).equals(argName)) {
			return null;
		}
    	args.remove(index);
    	if (index < args.size()) {
    		String value = args.get(index);
    		args.remove(index);
    		return value;
    	}
    	throw new DlabException("Argument \"" + argName + "\" detected but not have value");
    }

    /** Return pair name/value separated.
     * @param argName name of argument.
     * @param value value.
     * @param separator separator.
     */
    private Pair<String, String> getPair(String argName, String value, String separator) {
    	String [] array = value.split(separator);
    	if (array.length != 2) {
    		throw new DlabException("Invalid value for \"" + argName + "\": " + value);
    	}
    	return new ImmutablePair<String, String>(array[0], array[1]);
    }

    /** Parse command line.
     * @param cmd command line.
     */
    public void parse(String cmd) {
        json = null;
        command = null;
        action = null;
        environment.clear();
        variable.clear();
        others.clear();

    	List<String> args = extractArgs(cmd);
    	int i = 0;
    	String s;
    	Pair<String, String> p;

        while (i < args.size()) {
    		if ((s = getArgValue(args, i, "-v")) != null) {
    			p = getPair("-v", s, ":");
    			variable.put(p.getValue(), p.getKey());
    		} else if ((s = getArgValue(args, i, "-e")) != null) {
    			p = getPair("-e", s, "=");
    			environment.put(p.getKey(), p.getValue());
    		} else if ((s = getArgValue(args, i, "docker")) != null) {
    			command = s;
    		} else if ((s = getArgValue(args, i, "--action")) != null) {
    			action = s;
    		} else if ((s = getArgValue(args, i, "--name")) != null) {
    			name = s;
    		} else if ((s = getArgValue(args, i, "echo")) != null) {
    			if (s.equals("-e")) {
    				if (i >= args.size()) {
        		    	throw new DlabException("Argument \"echo -e\" detected but not have value");
    		    	} 
		    		s = args.get(i);
		    		args.remove(i);
    			}
    			json = s;
    		} else {
    			i++;
    		}
    	}
    	
    	if (args.size() > 0) {
    		others.addAll(args);
    	}
    }
    
    @Override
    public String toString() {
    	return MoreObjects.toStringHelper(this)
    			.add("command", command)
    			.add("action", action)
    			.add("name", name)
    			.add("environment", environment)
    			.add("variable", variable)
    			.add("others", others)
    			.add("json", json)
    			.toString();
    }
}
