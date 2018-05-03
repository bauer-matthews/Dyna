package parser.cli;

import org.apache.commons.cli.*;
import util.ExitCode;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * SPAN - Stochastic Protocol Analyzer
 * <p>
 * Created: 5/22/17
 *
 * @author Matthew S. Bauer
 * @version 1.0
 */
public class OptionsParser {

    public static void parse(String[] args) {

        CommandLineParser parser = new DefaultParser();

        // First parse the option list of help/empty to avoid conflicts with required options
        try {

            CommandLine cmd = parser.parse(CLIOptionsHelper.getOptions(), args, false);
            List<Option> optionsList = Arrays.asList(cmd.getOptions());

            for (Option option : CLIOptionsHelper.getHelpOptions().getOptions()) {
                if (optionsList.contains(option)) {
                    CLIOptionsHelper.printUsage();
                    System.exit(ExitCode.CLI_OPTION_ERROR.getValue());
                }
            }
        } catch (ParseException ex) {
            // Bury exception, parse next options group
        }

        try {
            CommandLine cmd = parser.parse(CLIOptionsHelper.getOptions(), args);
            List<Option> options = Arrays.asList(cmd.getOptions());
            initializeRunConfiguration(options);
            CLIOptionsHelper.validateOptions(options);
        } catch (ParseException ex) {
            CLIOptionsHelper.printParseError(ex);
            CLIOptionsHelper.printUsage();
            System.exit(ExitCode.CLI_OPTION_ERROR.getValue());
        }
    }

    private static void initializeRunConfiguration(List<Option> options) {
        Consumer<Option> optionConsumer = CLIOptionsHelper.getOptionConsumer();
        for (Option option : options) {
            optionConsumer.accept(option);
        }
    }
}
