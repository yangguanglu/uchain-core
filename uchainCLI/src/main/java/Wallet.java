import com.uchain.common.Serializabler;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.CryptoUtil;
import com.uchain.crypto.PrivateKey;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Getter
@Setter
public class Wallet {
    String name;
    String address;
    String privKey;
    String version;

    private Wallet(){}

    public Wallet(String name, String address, String privKey, String version){
        this.name = name;
        this.address = address;
        this.privKey = privKey;
        this.version = version;
    }

    public String readResources() {
        InputStream fileInputStream = null;
        String s = "";
        try {
            fileInputStream = this.getClass().getResourceAsStream("wallet.json");
            byte[] data = new byte[1024];
            int i = 0;
            int n = fileInputStream.read();
            while (n != -1){
                data[i] = (byte)n;
                i++;
                n = fileInputStream.read();
            }
            s = new String(data, 0 ,i);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                fileInputStream.close();
            }
            catch (Exception e){}
            return s;
        }

    }

    public void generateNewPrivKey(){
        val key = PrivateKey.apply(CryptoUtil.array2binaryData(Crypto.randomBytes(32)));
        privKey = key.toWIF();
        address = key.publicKey().toAddress();
    }

    public String getPublicKey(){

        //PrivateKey privateKey = PrivateKey.apply(CryptoUtil.fromHexString(privKey));
        PrivateKey privateKey = PrivateKey.fromWIF(privKey);
        String address = privateKey.publicKey().toAddress();
        return address;
    }

    public boolean isValide(String walletJson){
        if(walletJson == null || walletJson.isEmpty()) return false;
        try {
            Wallet wallet = Serializabler.JsonMapperFrom(walletJson, Wallet.class);

            return !(wallet.address.isEmpty() || wallet.privKey.isEmpty()
                    || wallet.name.isEmpty() || wallet.version.isEmpty());
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return "{" + "\"name\" : \"" + name + "\", \"address\" : \"" + address +"\", \"privKey\" :\"" + privKey + "\", \"version\" : \""+version+"\"}";
    }
}
