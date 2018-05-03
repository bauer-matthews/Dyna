package parser.cli;

import org.apache.commons.cli.Option;
import org.jboss.util.collection.ConcurrentReferenceHashMap;

import java.util.function.Consumer;

/**
 * SPAN - Stochastic Protocol Analyzer
 * <p>
 * Created: 10/30/17
 *
 * @author Matthew S. Bauer
 * @version 1.0
 */
public class CLIOptions {

    // Debug Option
    static Option debug = new Option("debug", "print debugging information");
    static Consumer<Option> debugConsumer = (debugOpt) -> {};

    // Trace Option
    static Option trace = new Option("trace", "print path exploration");
    static Consumer<Option> traceConsumer = (traceOpt) -> {};

//    // Instrument Option
//    static Option mode = Option.builder("mode")
//            .desc("the rum mode")
//            .hasArg()
//            .argName("mode")
//            .required(true)
//            .build();
//    static Consumer<Option> modeConsumer = (instrumentOpt) -> {
//        switch (instrumentOpt.getValue()) {
//            case "instrument" :
//                RunConfiguration.setRunMode(MODE.INSTRUMENT);
//                break;
//            case "igoodlock" :
//                RunConfiguration.setRunMode(MODE.IGOODLOCK);
//                break;
//            default: throw new IllegalArgumentException("unrecognized run mode");
//        }
//    };

}
