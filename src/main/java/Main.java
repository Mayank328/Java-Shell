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
            
            List<String> command_list = new ArrayList<>(Arrays.asList("echo","exit","type","pwd"));

            String pathEnv = System.getenv("PATH");
            String pathSeparator = System.getProperty("path.separator");
            String fileSeparator = System.getProperty("file.separator");
            String[] paths = pathEnv.split(pathSeparator);
            
            switch(input){
                case "exit 0":
                    break outerLoop;
                case "pwd":
                    System.out.println(System.getProperty("user.dir"));
                    break;
                default:
                    if (input.startsWith("type")){
                        String commandName = input.substring(5,input.length());
                        if(command_list.contains(commandName)){
                            System.out.println(commandName + " is a shell builtin");
                        }
                        else{
                            boolean notFound = true;

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
                        
                    }
                    else if (input.startsWith("echo")) {
                        System.out.println(input.substring(5,input.length()));
                    }
                    else{
                        String[] command_and_args = input.trim().split("\\s+");
                        String command = command_and_args[0];
                        String[] command_args = Arrays.copyOfRange(command_and_args,1,command_and_args.length);
                    
                        String executable = null;

                        for (String dir: paths){
                            File file = new File(dir,command);
                            if(file.exists() && file.canExecute()){
                                executable = file.getName();
                                break;
                            }
                        }
                        if(executable == null){
                            System.out.println(command + ": command not found");
                        }else{
                            List<String> curr_command_list = new ArrayList<>();
                            curr_command_list.add(executable);
                            curr_command_list.addAll(Arrays.asList(command_args));

                            ProcessBuilder pb = new ProcessBuilder(curr_command_list);
                            pb.inheritIO();
                            try {
                                Process process = pb.start();
                                process.waitFor();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            }

        }
    }
}
