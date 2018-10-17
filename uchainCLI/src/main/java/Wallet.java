import com.uchain.crypto.BinaryData;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.CryptoUtil;
import com.uchain.crypto.PrivateKey;
import lombok.val;

public class Wallet {
    String name;
    String address;
    String privKey;
    String version;

    public Wallet(String name, String address, String privKey, String version){
        this.name = name;
        this.address = address;
        this.privKey = privKey;
        this.version = version;
    }

    public void generateNewPrivKey(){
        val key = PrivateKey.apply(CryptoUtil.array2binaryData(Crypto.randomBytes(32)));
        privKey = key.toWIF();
        address = key.publicKey().toAddress();
    }
}
