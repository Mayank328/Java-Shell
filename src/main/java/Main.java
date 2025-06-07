import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

        public static List<String> parseInput(String input) {

        List<String> outputs = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = '\0';

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar == '\\' && i < input.length() - 1) {
                builder.append(input.charAt(++i));
                continue;
            }

            if ((currentChar == '"' || currentChar == '\'') && !inQuote) {
                inQuote = true;
                quoteChar = currentChar;
            } else if (currentChar == quoteChar && inQuote) {
                inQuote = false;
                quoteChar = '\0';
            } else if (!inQuote && Character.isWhitespace(currentChar)) {
                if (builder.length() > 0) {
                outputs.add(builder.toString());
                builder.setLength(0);
                }
            } else {
                builder.append(currentChar);
            }
        }

        if (builder.length() > 0) {
            outputs.add(builder.toString());
        }
        System.out.println(builder);
        return outputs;
    }
    public static void main(String[] args) throws Exception {
        
        File currentDir = new File(System.getProperty("user.dir"));
        List<String> command_list = new ArrayList<>(Arrays.asList("echo","exit","type","pwd"));

        String pathEnv = System.getenv("PATH");
        String pathSeparator = System.getProperty("path.separator");
        String fileSeparator = System.getProperty("file.separator");
        String[] paths = pathEnv.split(pathSeparator);

        while (true) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            if(input.equals("exit 0")) break;

            List<String> command_and_args = parseInput(input);
            System.out.println(command_and_args);

            String command = command_and_args.get(0);
            List<String> command_args_list = command_and_args.subList(1,command_and_args.size());
            String[] command_args = command_args_list.toArray(new String[0]);
            
            switch(command){
                case "pwd":
                    System.out.println(currentDir);
                    break;
                default:
                    if (command.equals("type")){
                        String argName = String.join(" ",command_args);
                        if(command_list.contains(argName)){
                            System.out.println(argName + " is a shell builtin");
                        }
                        else{
                            boolean notFound = true;

                            for (String dir : paths){
                                File file = new File(dir + fileSeparator + argName);
                                if(file.exists() && file.canExecute()){
                                    System.out.println(argName + " is " + file.getAbsoluteFile());
                                    notFound = false;
                                    break;
                                }
                            }
                            if (notFound){
                                System.out.println(argName + ": not found");
                            }
                        }
                        
                    }
                    else if (command.equals("echo")) {
                        System.out.println(String.join(" ",command_args));
                    }
                    else if (command.equals("cd")){
                        File newDir = new File(command_args[0]);

                        if (command_args[0].equals("~")){
                            newDir = new File(System.getenv("HOME"));
                        }
                        if(!newDir.isAbsolute()){
                            newDir = new File(currentDir,command_args[0]);
                        }
                        if(newDir.exists() && newDir.isDirectory()){
                            currentDir = newDir.getCanonicalFile();
                        }
                        else{
                            System.out.println(command+": "+ command_args[0] + ": No such file or directory");
                        }
                    }
                    else{
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
