import com.uchain.common.Serializabler;

import java.io.*;

public class WalletLoader {

    private final static String fileName = "wallet.json";

    public static Wallet load() {
        Wallet defaultWallet = new Wallet("", "", "", "0.1");
        defaultWallet.generateNewPrivKey();
        FileReader fr = null;
        try{
            fr =new FileReader(fileName);
            BufferedReader br=new BufferedReader(fr);
            String walletJson = "";
            String line="";
            while ((line =br.readLine())!=null) {
                walletJson += line;
            }

            if(defaultWallet.isValide(walletJson))
                defaultWallet = Serializabler.JsonMapperFrom(walletJson, Wallet.class);
            //save(defaultWallet);

        }catch (IOException e){
            save(defaultWallet);
        }finally {
            try {
                if(fr != null)
                    fr.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
/*
        String jsonStr = defaultWallet.readResources();
        try {
            if(! jsonStr.isEmpty()){
                defaultWallet = Serializabler.JsonMapperFrom(jsonStr, Wallet.class);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            FileOutputStream outputStream = null;
            try {
                outputStream =  new FileOutputStream("uchainCLI\\src\\main\\resources\\wallet.json");
                String string = Serializabler.JsonMapperTo(defaultWallet);
                outputStream.write(string.getBytes());
                outputStream.close();
            }
            catch (Exception e1){
                e1.printStackTrace();
                try {
                    outputStream.close();
                }
                catch (Exception e2){}
            }

        }
        finally {
            return defaultWallet;
        }*/

        return defaultWallet;
    }

    private static void save(Wallet wallet){
        FileWriter fw = null;
        try {
            fw = new FileWriter(fileName);
            fw.write(wallet.toString());
            fw.close();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                fw.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
