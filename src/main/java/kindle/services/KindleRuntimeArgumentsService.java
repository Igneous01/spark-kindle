package kindle.services;

import kindle.domain.KindleArguments;
import kindle.exceptions.KindleLauncherException;

import java.util.OptionalInt;
import java.util.stream.IntStream;

public class KindleRuntimeArgumentsService {

    private final String ARGUMENT_MATCHER_NAME = "--kindleArgumentMatcher";
    private final String ARGUMENT_REQUEST_NAME = "--kindleRequest";
    private final String ARGUMENT_APPARGS_NAME = "--appArgs";
    private KindleAppArgumentParserService kindleAppArgumentParserService = new KindleAppArgumentParserService();

    public KindleArguments processArgs(String[] args) {
        String request = getArgumentValueForName(ARGUMENT_REQUEST_NAME, args);
        String matcher = getArgumentValueForName(ARGUMENT_MATCHER_NAME, args);
        String appArgsRawValue = getArgumentValueForName(ARGUMENT_APPARGS_NAME, args);
        String[] appArgs = null;
        if (appArgsRawValue != null && !appArgsRawValue.isEmpty()) {
            if (kindleAppArgumentParserService.isValid(appArgsRawValue)) {
                appArgs = kindleAppArgumentParserService.extract(appArgsRawValue);
            }
            else {
                throw new KindleLauncherException("--appArgs are malformed - surround arguments with ${ }");
            }
        }
        return new KindleArguments(request, matcher, appArgs);
    }

    private String getArgumentValueForName(String argumentName, String[] args) {
        int argsLength = args.length;
        OptionalInt argIndex = IntStream
                .range(0, argsLength)
                .filter(index-> args[index].equals(argumentName))
                .findFirst();

        if (argIndex.isPresent()) {
            int argValueIndex = argIndex.getAsInt() + 1;
            if (argValueIndex > (argsLength - 1) ) {
                throw new KindleLauncherException("No arguments provided for " + argumentName + " - args must be provided after " + argumentName);
            }
            else {
                return args[argValueIndex];
            }
        }
        else {
            return null;
        }
    }
}
