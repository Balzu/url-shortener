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
		help = OptionBuilder.withLongOpt("help").create('h');		
		options = new Options();
		options.addOption(config);
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
	
	public boolean needHelp(){
		if (line.hasOption( help.getOpt()))
			return true;
		return false;
	}
	
	public void printHelp(){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(" [ c <path_to_config>] ", options );
	}
	
	

}
