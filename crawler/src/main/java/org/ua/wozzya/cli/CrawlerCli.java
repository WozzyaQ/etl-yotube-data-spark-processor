package org.ua.wozzya.cli;

import org.apache.commons.cli.*;

import java.io.PrintStream;
import java.io.PrintWriter;

public class CrawlerCli {
    public enum CmdOption {
        OUTPUT_DIRECTORY("o",
                "outdir",
                true,
                1,
                "output directory for storing crawled data",
                false,
                "path"),
        INPUT_DIRECTORY(
                "i",
                "inputdir",
                true,
                1,
                "input directory",
                false,
                "path"
        ),
        API_KEY("k",
                "apiKey",
                true,
                1,
                "youtube api key",
                false,
                "api-key");


        Option option;

        CmdOption(String shortName,
                  String longName,
                  boolean hasArguments,
                  int amountOfArguments,
                  String description,
                  boolean isOptional,
                  String argumentName) {
            Option option = new Option(shortName, longName, hasArguments, description);
            option.setOptionalArg(isOptional);
            option.setArgs(amountOfArguments);
            option.setArgName(argumentName);
            this.option = option;
        }

        public Option getOption() {
            return option;
        }
    }

    private CommandLine cmd;
    private Options options;
    private final String[] cmdArguments;

    public CrawlerCli(String[] cmdArguments) {
        this.cmdArguments = cmdArguments;
        initCmd();

    }


    public boolean hasOption(CmdOption cmdOption) {
        return cmd.hasOption(cmdOption.option.getOpt());
    }

    public String getOptionValue(CmdOption cmdOption, boolean required) {
        String value = cmd.getOptionValue(cmdOption.option.getOpt());

        if (value == null && required) {
            printUsageDefault();
            throw new RuntimeException(cmdOption.option.getOpt() + " value is required");
        }

        return value;
    }

    private void printUsageDefault() {
        printUsage(
                80,
                "Options",
                "--HELP--",
                3,
                5,
                true,
                System.out
        );
    }

    private void initCmd() {
        Options options = new Options();
        for (CmdOption cmdOption : CmdOption.values()) {
            options.addOption(cmdOption.getOption());
        }

        this.options = options;
        try {
            cmd = new DefaultParser().parse(options, cmdArguments);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printUsageDefault();
            throw new RuntimeException(e);
        }
    }

    public void printUsage(int rowWidth,
                           String header,
                           String footer,
                           int spaceBeforeOption,
                           int spaceBeforeOptionDescription,
                           boolean displayUsage,
                           PrintStream out) {
        String cmdSyntax = "java Crawler.jar";
        PrintWriter writer = new PrintWriter(out);
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                rowWidth,
                cmdSyntax,
                header,
                options,
                spaceBeforeOption,
                spaceBeforeOptionDescription,
                footer,
                displayUsage
        );

        writer.flush();
    }
}
