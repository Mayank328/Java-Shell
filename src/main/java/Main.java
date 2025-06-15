import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.Completer;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;


class InputClass{

        static {
            try {
                // Terminal terminal = TerminalBuilder.builder().system(true).build();

               boolean isCI = System.getenv("CI") != null || System.getenv("CODECRAFTERS_SUBMISSION") != null;

                TerminalBuilder builder = TerminalBuilder.builder();

                if (isCI) {
                    builder.dumb(true).system(false).streams(System.in, System.out);
                }

                Terminal terminal = builder.build();

                Completer completer = new StringsCompleter(TypeClass.command_list);

                reader = LineReaderBuilder.builder().terminal(terminal).completer(completer).parser(new DefaultParser()).build();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        static String input;
        static LineReader reader;

        static String command;
        static List<String> command_args_list;
        static String[] command_args;

        static boolean stdout_redirect = false;
        static boolean stdout_append = false;

        static boolean stderr_redirect = false;
        static boolean stderr_append = false;


        static List<String> command_and_args;
        static String outputFile = null;
        static String errorFile = null;
        
        public static void input(){
            // System.out.print("$ ");
            // Scanner scanner = new Scanner(System.in);
            // input = scanner.nextLine();

            input = reader.readLine("$ ");

            String[] parts = new String[] {input};
            String commandPart = input;

            if(input.contains("1>>")){

                parts = input.split("1>>",2);

                commandPart = parts[0].trim();
                outputFile = parts[1].trim();

                stdout_append = !stdout_append;


            }else if (input.contains("1>")) {

                parts = input.split("1>",2);

                commandPart = parts[0].trim();
                outputFile = parts[1].trim();

                stdout_redirect = !stdout_redirect;

            }else if(input.contains("2>>")){

                parts = input.split("2>>",2);

                commandPart = parts[0].trim();
                errorFile = parts[1].trim();

                stderr_append = !stderr_append;

            }else if(input.contains("2>")){

                parts = input.split("2>",2);
                
                commandPart = parts[0].trim();
                errorFile = parts[1].trim();

                stderr_redirect = !stderr_redirect;

            }else if(input.contains(">>")){

                parts = input.split(">>",2);

                commandPart = parts[0].trim();
                outputFile = parts[1].trim();

                stdout_append = !stdout_append;                

            }else if(input.contains(">")){

                parts = input.split(">",2);

                commandPart = parts[0].trim();
                outputFile = parts[1].trim();

                stdout_redirect = !stdout_redirect;
            }

            command_and_args = ParseClass.parseInput(commandPart);

            command = command_and_args.get(0);
            command_args_list = command_and_args.subList(1,command_and_args.size());
            command_args = command_args_list.toArray(new String[0]);

        }
}

class ParseClass{

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

class DirectoryClass{

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

class TypeClass{
        static List<String> command_list = new ArrayList<>(Arrays.asList("echo","exit","type","pwd"));
        static String argName;

        public static boolean builtin_check(String[] command_args){
            argName = String.join(" ",command_args);
            return command_list.contains(argName);
        }
}

class ExecutableClass{
        
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
                
                if(InputClass.outputFile != null){

                    if(InputClass.stdout_redirect){

                        pb.redirectOutput(new File(InputClass.outputFile).getAbsoluteFile());
                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                        InputClass.stdout_redirect = !InputClass.stdout_redirect;

                    }

                    if(InputClass.stdout_append){

                        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(InputClass.outputFile).getAbsoluteFile()));
                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                        InputClass.stdout_append = !InputClass.stdout_append;

                    }

                    InputClass.outputFile = null;

                }else if (InputClass.errorFile != null) {

                    if(InputClass.stderr_redirect){

                        pb.inheritIO();
                        pb.redirectError(new File(InputClass.errorFile).getAbsoluteFile());
                        InputClass.stderr_redirect = !InputClass.stderr_redirect;

                    }

                    if(InputClass.stderr_append){

                        pb.inheritIO();
                        pb.redirectError(ProcessBuilder.Redirect.appendTo(new File(InputClass.errorFile).getAbsoluteFile()));
                        InputClass.stderr_append = !InputClass.stderr_append;
                    }

                    InputClass.errorFile = null;

                }else{
                    pb.inheritIO();
                }
                try {

                    Process process = pb.start();
                    process.waitFor();

                } catch (Exception e) {

                    e.printStackTrace();

                }
            }
        }
}
public class Main {

    public static void main(String[] args) throws Exception {

        outerLoop: while (true) {
            InputClass.input();

            if(InputClass.outputFile!= null || InputClass.errorFile!=null){
                ExecutableClass.execute(InputClass.command_args,InputClass.command);
                continue;
            }

            switch(InputClass.command){
                case "pwd" -> DirectoryClass.current_directory();
                case "exit" -> {
                    break outerLoop;
                }
                case "type" -> {
                    if(TypeClass.builtin_check(InputClass.command_args)){
                        System.out.println(TypeClass.argName + " is a shell builtin");
                    }
                    else{
                        DirectoryClass.check_directory();
                    }
                }
                case "echo" -> System.out.println(String.join(" ",InputClass.command_args));
                case "cd" -> DirectoryClass.change_directory(InputClass.command_args,InputClass.command);
                default -> ExecutableClass.execute(InputClass.command_args,InputClass.command);
            }

        }
    }
}