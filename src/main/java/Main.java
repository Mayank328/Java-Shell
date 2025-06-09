import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public class InputClass{
        static String input;
        static List<String> command_and_args;
        
        public static void input(){
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            input = scanner.nextLine();
            command_and_args = ParseClass.parseInput(input);
        }
    }

    public class ParseClass{
        public static List<String> parseInput(String input) {

        // add space after input to added the last arg to list
        input = String.format("%s ", input);

        ArrayList<String> args = new ArrayList<>();

        StringBuilder sb = new StringBuilder("");
        int i = 0;

        while (i < input.length()) {
            char ch = input.charAt(i);
            char prev = i > 0 ? input.charAt(i - 1) : ' ';

            if (prev != '\\' && ch == ' ') {
                args.add(sb.toString());
                sb = new StringBuilder("");

                // ignore extra white spaces between args
                while (ch == ' ') {
                i++;
                if (i < input.length())
                    ch = input.charAt(i);
                else
                    break;
                }
            } else if (ch == '\\') {
                i++;
            } else if (prev != '\\' && ch == '\'') {
                // add everything untill another next single quote appears
                i++;

                while (i < input.length() - 1) {
                ch = input.charAt(i);

                if (ch == '\'') {
                    i++;
                    break;
                }
                sb.append(ch);
                i++;
                }
            } else if (prev != '\\' && ch == '\"') {
                i++;
                boolean isEscaped = false;
                while (i < input.length() - 1) {
                ch = input.charAt(i);

                if (!isEscaped && ch == '\"') {
                    i++;
                    break;
                }

                if (!isEscaped && ch == '\\') {
                    isEscaped = true;
                    i++;
                    continue;
                }

                if (isEscaped && (ch != '\\' && ch != '"')) {
                    sb.append("\\");
                }

                isEscaped = false;

                sb.append(ch);
                i++;
                }
            } else {
                sb.append(ch);
                i++;
            }
        }

            return args;
        }
    }

    public class DirectoryClass{
        static File currentDir = new File(System.getProperty("user.dir"));
        static String pathEnv = System.getenv("PATH");
        static String pathSeparator = System.getProperty("path.separator");
        static String fileSeparator = System.getProperty("file.separator");
        static String[] paths = pathEnv.split(pathSeparator);

        public static void current_directory(){
            System.out.println(currentDir);
        }

        public static void check_directory(){
            boolean notFound = true;

                for (String dir : paths){
                    File file = new File(dir + fileSeparator + TypeClass.argName);
                    if(file.exists() && file.canExecute()){
                        System.out.println(TypeClass.argName + " is " + file.getAbsoluteFile());
                        notFound = false;
                        break;
                    }
                }
            if (notFound){
                System.out.println(TypeClass.argName + ": not found");
            }
        }

        public static void change_directory(String[] command_args,String command) throws IOException{
            File newDir = new File(command_args[0]);

            if (command_args[0].equals("~")){
                newDir = new File(System.getenv("HOME"));
            }
            if(!newDir.isAbsolute()){
                newDir = new File(DirectoryClass.currentDir,command_args[0]);
            }
            if(newDir.exists() && newDir.isDirectory()){
                DirectoryClass.currentDir = newDir.getCanonicalFile();
            }
            else{
                System.out.println(command+": "+ command_args[0] + ": No such file or directory");
            }
        }
    }

    public class TypeClass{
        static List<String> command_list = new ArrayList<>(Arrays.asList("echo","exit","type","pwd"));
        static String argName;

        public static boolean builtin_check(String[] command_args){
            argName = String.join(" ",command_args);
            return command_list.contains(argName);
        }
    }

    public class ExecutableClass{
        
        public static void execute(String[] command_args,String command){
            String executable = null;

            for (String dir: DirectoryClass.paths){
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


    public static void main(String[] args) throws Exception {

        outerLoop: while (true) {
            InputClass.input();

            String command = InputClass.command_and_args.get(0);
            List<String> command_args_list = InputClass.command_and_args.subList(1,InputClass.command_and_args.size());
            String[] command_args = command_args_list.toArray(new String[0]);
            
            switch(command){
                case "pwd":
                    DirectoryClass.current_directory();
                    break;
                case "exit":
                    break outerLoop;
                case "type":
                    if(TypeClass.builtin_check(command_args)){
                            System.out.println(TypeClass.argName + " is a shell builtin");
                    }
                    else{
                        DirectoryClass.check_directory();
                    }
                    break;
                case "echo":
                    System.out.println(String.join(" ",command_args));
                    break;
                case "cd":
                    DirectoryClass.change_directory(command_args,command);
                    break;
                default:
                    ExecutableClass.execute(command_args,command);
            }

        }
    }
}
