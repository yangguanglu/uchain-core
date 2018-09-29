import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
//@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Command {

    public String path;

    public List<CommandSuffix> commandSuffixes;

    public Command(){}



    public Command(String path, List<CommandSuffix> commandSuffixes){
        this.path = path;
        this.commandSuffixes = commandSuffixes;
    }

}
