package br.unb.cic.metrics;

import br.unb.cic.metrics.controller.DependencyManager;
import br.unb.cic.metrics.pp.DefaultPrinter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CCMetrics {
    private static final String PROJECT = "--project";
    private static final String INPUT = "--input";
    private static final String OUTPUT = "--output";
    private static final String MODE = "--mode";

    public static void main(String args[]) {
        try {
            HashMap<String, String> cmds = parseProgramArgs(args);

            List<String> projects = Files.readAllLines(Paths.get(cmds.get(INPUT)));

            projects.forEach(p -> {
               StringTokenizer tokenizer = new StringTokenizer(p, ";");
               String name = tokenizer.nextToken().trim();
               String log = tokenizer.nextToken().trim();

               System.out.println("Project name: " + name);
               System.out.println("Project log: " + log);

                DependencyManager mgr = run(log, cmds.containsKey(MODE) ? cmds.get(MODE) : "c");

               if(mgr != null) {
                   mgr.export(new DefaultPrinter(name, cmds.get(OUTPUT)));
               }
            });
        }
        catch(Throwable e) {
            try {
                PrintStream logger = new PrintStream(new File("error.txt"));
                e.printStackTrace(logger);
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static HashMap<String, String> parseProgramArgs(String[] args) {
        try {
            HashMap<String, String> cmds = new HashMap<>();

            String arr[] = {INPUT, OUTPUT, MODE, PROJECT};
            Set<String> validCommands = new HashSet<>(Arrays.asList(arr));

            if (args.length >= 2) {
                for (String cmd : args) {
                    StringTokenizer tokenizer = new StringTokenizer(cmd, "=");
                    String cmdName = tokenizer.nextToken();
                    String cmdValue = tokenizer.nextToken();

                    if (validCommands.contains(cmdName)) {
                        cmds.put(cmdName, cmdValue);
                    } else {
                        invalidCommandArgs();
                    }
                }
            }
            if (!(cmds.containsKey(INPUT) && cmds.containsKey(OUTPUT))) {
                invalidCommandArgs();
            }
            return cmds;
        }
        catch(Exception e) {
            invalidCommandArgs();
            return null;
        }
    }

    private static void invalidCommandArgs() {
        System.out.println("Invalid command options. Please, try\n  " +
                "java CCMetrics --project=<project_name> --input=<logfile> --output=<outputdir> (--mode=(c|f))?\n");

        System.out.println("The log file must have been generated using the following command (in a Kenja repository): " +
                "\n    git log --name-only --oneline --pretty='%ncommit: %H' > log.txt");

        System.exit(1);
    }

    private static DependencyManager run(String file, String mode) {
        try {
            DependencyManager builder = new DependencyManager();

            builder.setMode(mode == "c" ? DependencyManager.Mode.COARSE_GRAINED : DependencyManager.Mode.FINE_GRAINED);

            builder.loadDependencies(file);

            return builder;
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
