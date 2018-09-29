import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    static String url = "http://127.0.0.1:1943";

    public static String sendCommandToServer(String command){
        String httpResponse = "";
        try {
            Command commandToSend = constructCommandToSend(command);
            String path = commandToSend.path;
            String body = DataProcessor.JsonMapperTo(commandToSend.commandSuffixes);
            httpResponse = ApacheHttpClient.getWithUrl(path, body);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return httpResponse;
    }

    public static Command constructCommandToSend(String command){
        String[] commandSplits = command.trim().split(" ");
        if(commandSplits.length > 1 && commandSplits.length % 2 ==1) {
            String path = commandSplits[0];
            String[] commandSuffixesBeforeConstruct = getCommandSuffix(command).split("\\s+");
            List<CommandSuffix> commandSuffixesAfterConstruct = constructCommandSuffixesList(commandSuffixesBeforeConstruct);
            return new Command(path, commandSuffixesAfterConstruct);
        }
        else return new Command(command, new ArrayList<>());
    }

    public static  int checkCommand(String command){
        if(command.isEmpty()) return 404;
        if(command.split("\\s+").length > 1 && command.matches("^[a-zA-Z].*-.*")){
            if(!checkCommandHeadIsCorrect(command)) return 404;
            if(!checkCommandValid(command)) return 400;
            if(checkCommandValid(command)) return 200;
        }
        if(!command.contains("-") && supportedCommand.contains(command)) return 200;
        else return 400;
    }

    private static boolean checkCommandHeadIsCorrect(String command){
        String commandHead = command.split("\\-")[0].trim();
        return supportedCommand.contains(commandHead);
    }

    private static boolean checkCommandValid(String command){
        String commandHead = command.split("\\-")[0].trim();
        String commandParamsSuffix = getCommandSuffix(command);
        return checkSpecifiedCommandIsCorrect(commandParamsSuffix.split("\\s+"), commandHead);
    }

    private static boolean checkSpecifiedCommandIsCorrect(String[] paramsSuffix, String commandHead){
        boolean validateAllParamsCorrect = true;
        int cmdLength = paramsSuffix.length;
        if(cmdLength % 2 == 0){
            List<CommandSuffix> commandSuffixes = constructCommandSuffixesList(paramsSuffix);
            for(CommandSuffix commandSuffix: commandSuffixes){
                if(!validate(commandSuffix, commandHead)) {
                    validateAllParamsCorrect = false;
                }
            }
        }
        else validateAllParamsCorrect = false;
        return validateAllParamsCorrect;
    }

    public static List<CommandSuffix> constructCommandSuffixesList(String[] paramsSuffix){
        int cmdLength = paramsSuffix.length;
        List<CommandSuffix> commandSuffixes = new ArrayList<>(cmdLength);
        for(int params = 0; params < cmdLength; params++){
            commandSuffixes.add(new CommandSuffix(paramsSuffix[params], paramsSuffix[params + 1]));
            params++;
        }
        return commandSuffixes;
    }

    private static boolean validate(CommandSuffix commandSuffix, String commandHead){
        return true;
    }

    private static String getCommandPrefix(String command){
        return command.split("\\-")[0].trim();
    }

    private static String getCommandSuffix(String command){
        return command.replaceAll(getCommandPrefix(command), "").trim();
    }

    static List<String> supportedCommand  = getAllSupportedCommand();

    private static List<String> getAllSupportedCommand(){
        ArrayList<String> supportedCommandList = new ArrayList<String>(20);
        supportedCommandList.add("getblocks");
        supportedCommandList.add("getblock");
        supportedCommandList.add("getblockcount");
        supportedCommandList.add("produceblock");
        supportedCommandList.add("sendrawtransaction");
        supportedCommandList.add("gettx");
        supportedCommandList.add("importprivkey");
        supportedCommandList.add("quit");
        supportedCommandList.add("exit");
        return supportedCommandList;
    }

}
