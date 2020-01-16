package br.unb.cic.metrics.controller;

import br.unb.cic.metrics.model.Component;
import br.unb.cic.metrics.pp.PrettyPrinter;
import com.google.common.base.Stopwatch;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassDependencyBuilder {

    private static final int DEFAULT_MIN_SUPPORT_COUNT = 5;
    private static final double DEFAULT_MIN_CONFIDENCE = 0.5;

    private static final String PATTERN__COMMIT = "commit: (\\w++)";
    private static final String PATTERN__COMPONENT_COARSE_GRAINED =  "(.*)\\.java\\/\\[CN]\\/(\\w++)\\/.*";

    enum Mode {FINE_GRAINED, COARSE_GRAINED};

    private Mode mode = Mode.COARSE_GRAINED;
    private String projectName;
    private int minSupportCount = DEFAULT_MIN_SUPPORT_COUNT;
    private double minConfidence = DEFAULT_MIN_CONFIDENCE;

    private HashMap<String, Component> components = new HashMap<>();
    private HashMap<String, Set<String>> componentChanges = new HashMap<>();

    public void loadDependencies(String logFile) throws Exception {
        System.out.println("Loading commit history");
        Stopwatch stopwatch = Stopwatch.createStarted();

        components = new HashMap<>();
        componentChanges = new HashMap<>();

        List<String> log = Files.readAllLines(Paths.get(logFile));

        // map from a commit hash to the list of
        // change components
        HashMap<String, Set<String>> changeSet = new HashMap<>();

        int countCommit = 0;

        final Pattern commitPattern = Pattern.compile(PATTERN__COMMIT);
        final Pattern classPattern = Pattern.compile(PATTERN__COMPONENT_COARSE_GRAINED);
        String hash = "";

        for(String s : log) {
            Matcher m = commitPattern.matcher(s);
            if(m.matches()) {
                countCommit++;
                hash = m.group(1);
                // just being defensive here. it is not expected
                // to find the same hash twice in the log file.
                if(!changeSet.containsKey(hash)) {
                    changeSet.put(hash, new HashSet<>());
                }
            }
            else {
                m = classPattern.matcher(s);
                if (m.matches()) {
                    changeSet.get(hash).add(m.group(2));
                }
            }
        }
        stopwatch.stop();
        System.out.println(String.format(" total number of commits: %d" , countCommit));
        System.out.println(String.format(" Time elapsed: %d ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));

        buildDependencies(changeSet);
        pruneDependencies();
    }



    private void buildDependencies(HashMap<String, Set<String>> changeSet) {
        System.out.print("Building dependencies");
        Stopwatch stopwatch = Stopwatch.createStarted();

        for(String k : changeSet.keySet()) {
            for(String c: changeSet.get(k)) {
                Set<String> commits = componentChanges.containsKey(c) ? componentChanges.get(c) : new HashSet<>();
                commits.add(k);
                componentChanges.put(c,commits);
                createComponentDependencies(c, changeSet.get(k));
            }
        }
        stopwatch.stop();
        System.out.println(String.format(" Time elapsed: %d ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }

    private void createComponentDependencies(String name, Set<String> deps) {
        Component component = components.containsKey(name) ? components.get(name) : new Component(name);
        component.reportChange();
        deps.forEach(d -> {
            if (!name.equals(d)) {
                component.addCoChangeDependecy(d);
            }
        });
        components.put(name, component);
    }

    private void pruneDependencies() {
        System.out.print("Pruning dependencies");
        Stopwatch stopwatch = Stopwatch.createStarted();
        components.values().forEach(c -> c.pruneDependencies(minSupportCount, minConfidence));
        stopwatch.stop();
        System.out.println(String.format(" Time elapsed: %d ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }

    public void export(PrettyPrinter pp) {
        pp.exportComponentChanges(componentChanges);
        pp.exportComponentDependencies(components);
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setMinSupportCount(int minSupportCount) {
        this.minSupportCount = minSupportCount;
    }

    public void setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }
}

