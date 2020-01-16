package br.unb.cic.metrics;

import br.unb.cic.metrics.controller.ClassDependencyBuilder;
import br.unb.cic.metrics.pp.DefaultPrinter;

import java.util.HashMap;
import java.util.Set;

public class CCMetrics {
    public static void main(String args[]) {
        try {
            if(args.length < 2) {
                System.out.println("Invalid command options. Please, try java CCMetrics <logfile> <outputdir>");
                System.out.println("The log file must have been generated using the following command (in a Kenja repository): " +
                        " git log --name-only --oneline --pretty='%ncommit: %H' > log.txt");

                System.exit(1);
            }

            ClassDependencyBuilder builder = new ClassDependencyBuilder();
            builder.setMode(ClassDependencyBuilder.Mode.FINE_GRAINED);
            builder.loadDependencies(args[0]);
            builder.export(new DefaultPrinter(args[1]));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
