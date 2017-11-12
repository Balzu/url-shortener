package it.unipi.mcsn.pad.core;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CoreCommandLineManager {
	
	private Option config;
	private Option node;
	private Option help;
	private Options options;
	private CommandLineParser parser;
	private CommandLine line;
	
	public CoreCommandLineManager(String [] args) throws ParseException
	{
		config   = OptionBuilder.withLongOpt("config")
				.withArgName( "path" )
                .hasArg()
                .withDescription( "path to the custom configuration file 'core.conf'" )
                .create( "c" );
		node   = OptionBuilder.withLongOpt("node")
				.withArgName( "node_id" )
                .hasArg()
                .withDescription(  "turns on the node in the cluster with the given id" )
                .create( "n" );
		help = OptionBuilder.withLongOpt("help").withDescription("Display usage information").create('h');		
		options = new Options();
		options.addOption(config);
		options.addOption(node);
		options.addOption(help);
		parser = new DefaultParser();
	    line = parser.parse( options, args );
	}
	
	public boolean hasConfigFile(){
		if (line.hasOption( config.getOpt()))
			return true;
		return false;
	}
	
	public String getConfigurationPath(){
		return line.getOptionValue(config.getOpt());
	}
	
	public boolean hasNode(){
		if (line.hasOption( node.getOpt()))
			return true;
		return false;
	}
	
	public String getNode(){
		return line.getOptionValue(node.getOpt());
	}
	
	public boolean needHelp(){
		if (line.hasOption( help.getOpt()))
			return true;
		return false;
	}
	
	public void printHelp(){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(" -n <node_id> [ -c <path_to_config>] ", options );
	}
	
	

}
