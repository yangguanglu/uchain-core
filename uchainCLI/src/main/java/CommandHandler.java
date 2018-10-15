import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    static String url = "http://172.16.12.43:1943";

    public String sendCommandToServer(String command){
        String httpResponse = "";
        try {
            if(command.contains("help")) return Help.help;
            Command commandToSend = constructCommandToSend(command);
            String path = commandToSend.path;
            String body = DataProcessor.JsonMapperTo(commandToSend.commandSuffixes);
            System.out.println("**************");
            System.out.println(body);
            httpResponse = ApacheHttpClient.getWithUrl(path, body);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return httpResponse;
    }

    public String readResourceTxt(String file){
            StringBuilder result = new StringBuilder();
            try {
                //String filePath = this.getClass().getClassLoader().getResource(file).getFile();
                BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(new File("../../resources/help.txt")), "UTF-8"));
                String lineTxt = null;
                while ((lineTxt = bfr.readLine()) != null) {
                    result.append(lineTxt).append("\n");
                }
                bfr.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
    }

    public Command constructCommandToSend(String command){
        String[] commandSplits = command.trim().split(" ");
        if(commandSplits.length > 1 && commandSplits.length % 2 ==1) {
            String path = commandSplits[0];
            String[] commandSuffixesBeforeConstruct = getCommandSuffix(command).split("\\s+");
            List<CommandSuffix> commandSuffixesAfterConstruct = constructCommandSuffixesList(commandSuffixesBeforeConstruct);
            return new Command(path, commandSuffixesAfterConstruct);
        }
        else return new Command(command, new ArrayList<>());
    }

    public int checkCommand(String command){
        if(command.isEmpty()) return 404;
        if(command.split("\\s+").length > 1 && command.matches("^[a-zA-Z].*-.*")){
            if(!checkCommandHeadIsCorrect(command)) return 404;
            if(!checkCommandValid(command)) return 400;
            if(checkCommandValid(command)) return 200;
        }
        if(!command.contains("-") && supportedCommand.contains(command)) return 200;
        else return 400;
    }

    private  boolean checkCommandHeadIsCorrect(String command){
        String commandHead = command.split("\\-")[0].trim();
        return supportedCommand.contains(commandHead);
    }

    private boolean checkCommandValid(String command){
        String commandHead = command.split("\\-")[0].trim();
        String commandParamsSuffix = getCommandSuffix(command);
        return checkSpecifiedCommandIsCorrect(commandParamsSuffix.split("\\s+"), commandHead);
    }

    private boolean checkSpecifiedCommandIsCorrect(String[] paramsSuffix, String commandHead){
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

    public List<CommandSuffix> constructCommandSuffixesList(String[] paramsSuffix){
        int cmdLength = paramsSuffix.length;
        List<CommandSuffix> commandSuffixes = new ArrayList<>(cmdLength);
        for(int params = 0; params < cmdLength; params++){
            commandSuffixes.add(new CommandSuffix(paramsSuffix[params], paramsSuffix[params + 1]));
            params++;
        }
        return commandSuffixes;
    }

    private boolean validate(CommandSuffix commandSuffix, String commandHead){
        return true;
    }

    private String getCommandPrefix(String command){
        return command.split("\\-")[0].trim();
    }

    private String getCommandSuffix(String command){
        return command.replaceAll(getCommandPrefix(command), "").trim();
    }

    public List<String> supportedCommand  = getAllSupportedCommand();

    private List<String> getAllSupportedCommand(){
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
        supportedCommandList.add("help");
        return supportedCommandList;
    }


}

class Help{
    static String help = "APEX NETWORK\n" +
            "\n" +
            "name            parameter                      description\n" +
            "getblocks       []                             list block\n" +
            "sendrawtransaction [-privkey,-address,-assetId,-amount,-nonce] transfer money\n" +
            "getblock        [-id]                          get block by id\n" +
            "                [-h]                           get block by height\n" +
            "gettx           [-id]                          get transaction\n" +
            "importprivkey   [-key]                         import private key\n" +
            "getblockcount   []                             get block count\n" +
            "produceblock    []                             produce block";
}
