import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.uchain.common.Serializabler;
import com.uchain.core.Account;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.*;
import com.uchain.crypto.CryptoUtil;
import lombok.val;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {

    static String url = "http://172.16.12.43:1943";

    public String sendCommandToServer(String command, Wallet wallet){
        String httpResponse = "";
        try {
            if(command.contains("help")) return Help.help;
            if(command.contains("newaddr")) return generateAddr();
            if(command.contains("walletinfo")) return getWalletInfo();
            if(command.trim().split("\\s+")[0].equals("send")) return sendDefaultTransaction(command, wallet);
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

    public String generateAddr(){

        BinaryData privKeyBin = CryptoUtil.array2binaryData(Crypto.randomBytes(32));
//
//        BinaryData privKeyBin = CryptoUtil.fromHexString(commandSuffixes.get(0).getSuffixValue());
//        Crypto.randomBytes(32)
//
        val privKey = PrivateKey.apply(privKeyBin);
        System.out.println(privKey.publicKey().toAddress());
        System.out.println(privKey.toWIF());
//        println(s"Address: ${privKey.publicKey.toAddress}")
//        println(s"Private key: ${privKey.toWIF}")
        return "";
    }

    public String getWalletInfo(){
        Wallet wallet = WalletLoader.load();
        String address = wallet.getPublicKey();
        System.out.println(address);
        return "";
    }

    public String sendDefaultTransaction(String command, Wallet wallet){
        Command commandToSend = constructCommandToSend("getaccount -address " + wallet.getPublicKey());
        String path = commandToSend.path;
        try {
            String body = DataProcessor.JsonMapperTo(commandToSend.commandSuffixes);
            String httpResponse = ApacheHttpClient.getWithUrl(path, body);

            String accountNonce = getAccount(httpResponse);
            String nonce = String.valueOf(accountNonce);

            String privkey = PrivateKey.fromWIF(wallet.privKey).toBin().toString();
            String address = command.trim().split("\\s+")[2];
            String assetId = "0000000000000000000000000000000000000000000000000000000000000000";
            String amount = command.trim().split("\\s+")[4];

            String sendTransCommand = "sendrawtransaction "+"-privkey "+ privkey + " -address "+ address + " -assetId " +
                    assetId + " -amount " + amount + " -nonce" + nonce;
            return sendCommandToServer(sendTransCommand, wallet);
        }
        catch (Exception e){
            e.printStackTrace();
            return "error occured";
        }
    }

    public String getAccount(String httpResponse){
        if(httpResponse.isEmpty()) return "";
        String[] accounts = httpResponse.split("nextNonce");
        String str = accounts[1];
        String nonce = str.split(",")[0];
        String[] nonce1 = nonce.split(":");
        return nonce1[nonce1.length -1];
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//
////        Type[] types = new Type[1];
////        JavaType javaType = mapper.getTypeFactory().constructCollectionType(LinkedHashMap.class, UInt256.class, Fixed8.class);
////        final ParameterizedTypeImpl type = ParameterizedTypeImpl.make()
//        JavaType javaType = mapper.getTypeFactory().constructParametricType(Account.class, ((LinkedHashMap<UInt256, Fixed8>).getContentType()).getClass());
//        try {
//            return mapper.readValue(httpResponse, new TypeReference<Map<UInt256, Fixed8>>() {});
//        }
//        catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
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
        supportedCommandList.add("newaddr");
        supportedCommandList.add("getaccount");
        supportedCommandList.add("walletinfo");
        supportedCommandList.add("send");
        return supportedCommandList;
    }


}

class Help{
    static String help = "APEX NETWORK\n" +
            "\n" +
            "name            parameter                      description\n" +
            "getaccount      [-address]                     get account\n" +
            "getblocks       []                             list block\n" +
            "sendrawtransaction [-privkey,-address,-assetId,-amount,-nonce] send raw transaction\n" +
            "newaddr         []                             create new address\n" +
            "getblock        [-id]                          get block by id\n" +
            "                [-h]                           get block by height\n" +
            "gettx           [-id]                          get transaction\n" +
            "importprivkey   [-key]                         import private key\n" +
            "walletinfo      []                             list wallet info\n" +
            "getblockcount   []                             get block count\n" +
            "send            [-to,-amount]                  transfer money";
}
