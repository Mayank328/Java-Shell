import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) throws Exception {

        outerLoop: while (true) {
            System.out.print("$ ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            
            List<String> command_list = new ArrayList<>(Arrays.asList("echo","exit","type"));
            
            switch(input){
                case "exit 0":
                    break outerLoop;
                default:
                    if (input.contains("type")){
                        String commandName = input.substring(5,input.length());
                        if(command_list.contains(commandName)){
                            System.out.println(commandName + " is a shell builtin");
                        }
                        else{
                            String pathEnv = System.getenv("PATH");
                            String pathSeparator = System.getProperty("path.separator");
                            String fileSeparator = System.getProperty("file.separator");
                            boolean notFound = true;

                            String[] paths = pathEnv.split(pathSeparator);
                            for (String dir : paths){
                                File file = new File(dir + fileSeparator + commandName);
                            if(file.exists() && file.canExecute()){
                                    System.out.println(commandName + " is " + file.getAbsoluteFile());
                                    notFound = false;
                                    break;
                                }
                            }
                            if (notFound){
                                System.out.println(commandName + ": not found");
                            }
                        }
                        
                        // String command = input.substring(5,input.length());
                        // if (command_list.contains(command)){
                        //     System.out.println(command + System.getenv("PATH"));
                        // }
                        // else{
                        //     System.out.println(command + ": not found");
                        // }
                    }
                    else if (input.contains("echo")) {
                        System.out.println(input.substring(5,input.length()));
                    }
                    else{
                        System.out.println(input + ": command not found");
                    }
            }

        }
    }
}
