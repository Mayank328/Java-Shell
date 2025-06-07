import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static List<String> parseInput(String input){
        
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;

        for(int i = 0; i<input.length(); i+=1){
            char c = input.charAt(i);

            if(c == '\\'){
                if(i+1 < input.length()){
                    char next = input.charAt(++i);

                    switch (next) {
                        case 'n': current.append('\n'); break;
                        case 't': current.append('\t'); break;
                        case 'r': current.append('\r'); break;
                        case 'b': current.append('\b'); break;
                        case 'f': current.append('\f'); break;
                        case '\\': current.append('\\'); break;
                        case '\'': current.append('\''); break;
                        case '\"': current.append('\"'); break;

                        case '0', '1', '2', '3','4', '5', '6', '7':
                            StringBuilder oct = new StringBuilder();
                            oct.append(next);
                            for(int j = 0; j<2 && i+1 <input.length();j+=1){
                                char oc = input.charAt(i+1);
                                if( oc>= '0' && oc <='7'){
                                    oct.append(oc);
                                    i+=1;
                                }else break;
                            }
                            current.append((char) Integer.parseInt(oct.toString(),8));
                            break;
                        default:
                            current.append(next); 
                    }
                }
                continue;
            }

            if(c == '\'' && !inDouble){
                inSingle = !inSingle;
                continue;
            }
            else if(c == '"' && !inSingle){
                inDouble = !inDouble;
                continue;
            }
            if (Character.isWhitespace(c) && !inSingle && !inDouble){
                if(current.length() > 0){
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else{
                current.append(c);
            }
        }
        if(current.length() > 0){
            tokens.add(current.toString());
        }
        return tokens;
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
