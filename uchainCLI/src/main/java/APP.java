import java.util.Scanner;

public class APP {
    public static void main(String[] args){
        System.out.println("Welcome to CLI, type \"help\" for command list:");
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine().toLowerCase().trim();
            int checkResult = CommandHandler.checkCommand(command);
            switch (checkResult){
                case 200:{
                    String result = CommandHandler.sendCommandToServer(command);
                    System.out.println(result);
                    break;
                }
                case 400:{
                    System.out.println("the command you input is invalid");
                    break;
                }
                case 404:{
                    System.out.println("the command is not found in supported commands list");
                    break;
                }
                case 409:{
                    System.out.println("the command is conflict");
                    break;
                }
                case 500:{
                    System.out.println("inner error occurs");
                    break;
                }
            }
        }
    }


}