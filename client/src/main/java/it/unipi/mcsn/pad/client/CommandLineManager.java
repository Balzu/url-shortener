package it.unipi.mcsn.pad.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineManager {
	
	private Option interactive;
	private Option put;
	private Option get;
	private Option remove;
	private Option config;
	private Option output;
	private Option help;
	private Options options;
	private CommandLineParser parser;
	private CommandLine line;
	
	
	
	public CommandLineManager(String [] args) throws ParseException{
		
		interactive = OptionBuilder.withLongOpt("interactive")
				.withDescription("start an interactive session")
				.create("i");					
		put   = OptionBuilder.withLongOpt("put")
				.withArgName( "url" )
                .hasArg()
                .withDescription(  "shorten the provided url" )
                .create( "p" );
		get   = OptionBuilder.withLongOpt("get")
				.withArgName( "short_url" )
                .hasArg()
                .withDescription(  "return the url associated to this short url, if any" )
                .create( "g" );
		remove   = OptionBuilder.withLongOpt("remove")
				.withArgName( "short_url" )
                .hasArg()
                .withDescription(  "removes the url associated to this short url, if any" )
                .create( "r" );
		config   = OptionBuilder.withLongOpt("config")
				.withArgName( "path" )
                .hasArg()
                .withDescription( "path to the custom configuration file 'client.conf'" )
                .create( "c" );
		output   = OptionBuilder.withLongOpt("output")
				.withDescription("store the response in an output file")
				.create("o");
		help = OptionBuilder.withLongOpt("help").create('h');
		
		options = new Options();
		options.addOption(interactive);
		options.addOption(put);
		options.addOption(get);
		options.addOption(remove);
		options.addOption(config);
		options.addOption(output);
		options.addOption(help);
		
	    parser = new DefaultParser();
	    line = parser.parse( options, args );
	}
	
	public CommandLine getCommandLine() {
		return line;
	}

	public boolean isInteractive(){
		return (line.hasOption( interactive.getOpt()));		
	}
	
	public boolean hasConfigFile(){
		return (line.hasOption( config.getOpt()));
	}
	
	public boolean hasOutputFile(){
		return (line.hasOption( output.getOpt()));
	}
	
	public String getOutputFileName() {
		return line.getOptionValue(output.getOpt());
	}
	
	public String getConfigurationPath(){
		return line.getOptionValue(config.getOpt());
	}
	
	/**
	 * Return the operation invoked by the client 
	 * @return The operation invoked from command line
	 * @throws ParseException, if the number of operations invoked is different from 1 
	 */
	public Option getOperation() throws ParseException{
		Option [] opts = line.getOptions();
		int count = 0;
		Option option = null;
		for (int i=0; i< opts.length; i++){
		   if (opts[i].getOpt() == interactive.getOpt() |
		     opts[i].getOpt() == put.getOpt() |
			 opts[i].getOpt() == get.getOpt() |
			 opts[i].getOpt() == remove.getOpt()){
			 count ++;
			 option = opts[i];						 
		   }
		 }		 
		 if (count != 1)
			 throw  new ParseException("Should specify one and only one option"
			 		+ " among put, get, remove and interactive");
		 else
			 return option;
	}
	
	public String getRemove(){
		return remove.getOpt();
	}
	
	public String getGet(){
		return get.getOpt();
	}
	
	public String getPut(){
		return put.getOpt();
	}
	
	public String getInteractive(){
		return interactive.getOpt();
	}
	
	public void printHelp(){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "{ -i | [ -p <long_url> | -g <short_url> | -r <short_url>] }"
				+ " [ -c <path_to_config>] [ -o <output_file>]", options );
	}
	
	public boolean needHelp(){
		if (line.hasOption( help.getOpt()))
			return true;
		return false;
	}
	
	

}
