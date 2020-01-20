package br.unb.cic.metrics.pp;

import br.unb.cic.metrics.model.Component;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DefaultPrinter implements PrettyPrinter {
    private PrintStream out1, out2, out3;

    private String name;

    public DefaultPrinter(String name) {
        this.name = name;
        out1 = System.out;
        out2 = System.out;
        out3 = System.out;
    }

    public DefaultPrinter(String name, String file) {
        try {
            this.name = name;
            File f = new File(file);
            if(f.exists() && f.isDirectory()) {
                out1 = new PrintStream(f + "/" + name + "-change-history.csv");
                out2 = new PrintStream(f + "/" + name + "-dependencies.csv");
                out3 = new PrintStream(f + "/" + name + "-metrics.csv");
            }
            else {
                defaultInit();
            }
        }
        catch(Exception e) {
            System.out.println(" error writing to file " + file);
            System.out.println(" exporting to stdout");
            defaultInit();
        }
    }

    public void defaultInit() {
        out1 = System.out;
        out2 = System.out;
        out3 = System.out;
    }

    @Override
    public void exportComponentChanges(HashMap<String, Set<String>> changeSet) {
        changeSet.keySet().forEach(k -> {
            changeSet.get(k).forEach(c -> out1.println(String.format("%s; %s; %s", name, k, c.replace("::", "; "))));
        });
    }

    @Override
    public void exportComponentDependencies(HashMap<String, Component> components, int minSupportCount) {
        components.values().forEach(c -> {
            List<String> deps = c.listDependencies();
            if(deps.size() > 0) {
                deps.forEach(s -> out2.println(name + "; " + s));
            }
            out3.println(String.format("%s; %s; %d; %d", name, c.getName(), c.numberOfCoupledComponents(minSupportCount), c.sumOfCoupling(minSupportCount)));
        });
    }
}
