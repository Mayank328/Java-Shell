import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage
        outerLoop: while (true) {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            
            List<String> command_list = new ArrayList<>(Arrays.asList("echo","exit"));
            
            switch(input){
                case "exit 0":
                    break outerLoop;
                default:
                    if (input.contains("type")){
                        String command = input.substring(5,input.length());
                        System.out.println(command + " is a shell builtin");
                    }
                    else if (input.contains("echo")) {
                        System.out.println(input.substring(5,input.length()));
                    }
                    else{
                        System.out.println(input + ": command not found");
                    }
            }
            
            // if( input.equals("exit 0") ){
            //     break;
            // }
            // else if(input.contains("echo")){
            //     System.out.println(input.substring(5,input.length()));
            // }
            // else{
            //     System.out.println(input + ": command not found");
            // }
        }
    }
}
