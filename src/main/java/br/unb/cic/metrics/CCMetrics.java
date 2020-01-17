package br.unb.cic.metrics;

import br.unb.cic.metrics.controller.DependencyManager;
import br.unb.cic.metrics.pp.DefaultPrinter;

import java.util.*;

public class CCMetrics {
    private static final String INPUT = "--input";
    private static final String OUTPUT = "--output";
    private static final String MODE = "--mode";

    public static void main(String args[]) {
        try {
            HashMap<String, String> cmds = parseProgramArgs(args);
            DependencyManager builder = new DependencyManager();

            if(cmds.containsKey(MODE)) {
                if(cmds.get(MODE).equals("f")) {
                    builder.setMode(DependencyManager.Mode.FINE_GRAINED);
                }
            }

            builder.loadDependencies(cmds.get(INPUT));
            builder.export(new DefaultPrinter(cmds.get(OUTPUT)));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> parseProgramArgs(String[] args) {
        try {
            HashMap<String, String> cmds = new HashMap<>();

            String arr[] = {INPUT, OUTPUT, MODE};
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
                "java CCMetrics --input=<logfile> --output=<outputdir> (--mode=(c|f))?\n");

        System.out.println("The log file must have been generated using the following command (in a Kenja repository): " +
                "\n    git log --name-only --oneline --pretty='%ncommit: %H' > log.txt");

        System.exit(1);
    }
}
