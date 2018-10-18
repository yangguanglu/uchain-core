import com.uchain.common.Serializabler;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class WalletLoader {
    public static Wallet load() {
        Wallet defaultWallet = new Wallet("", "", "", "0.1");
        defaultWallet.generateNewPrivKey();
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
        }
    }
}
