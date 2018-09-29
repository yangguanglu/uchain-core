import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CommandSuffix {

    private String suffixParam;

    private String suffixValue;

    CommandSuffix(){

    }

    public CommandSuffix(String suffixParam, String suffixValue){
        this.suffixParam = suffixParam;
        this.suffixValue = suffixValue;

    }
}
