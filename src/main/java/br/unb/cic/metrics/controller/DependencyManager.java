package br.unb.cic.metrics.controller;

import br.unb.cic.metrics.model.Component;
import br.unb.cic.metrics.model.DependencyInfo;
import br.unb.cic.metrics.pp.PrettyPrinter;
import com.google.common.base.Stopwatch;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DependencyManager {

    private static final int DEFAULT_MIN_SUPPORT_COUNT = 5;
    private static final double DEFAULT_MIN_CONFIDENCE = 0.5;

    private static final String PATTERN__COMMIT = "commit: (\\w++) (\\w++)";
    private static final String PATTERN__COMPONENT_COARSE_GRAINED =  "(.*)\\.java\\/\\[CN]\\/(\\w++)\\/.*";
    private static final String PATTERN__COMPONENT_FINE_GRAINED =  "(.*)\\.java\\/\\[CN]\\/(\\w++)\\/(\\[CT]|\\[MT])\\/(.*)\\/.*";


    public enum Mode {FINE_GRAINED, COARSE_GRAINED};

    private Mode mode = Mode.COARSE_GRAINED;

    private String projectName;

    private String patternComponent = PATTERN__COMPONENT_COARSE_GRAINED;

    private int minSupportCount = DEFAULT_MIN_SUPPORT_COUNT;
    private double minConfidence = DEFAULT_MIN_CONFIDENCE;

    private HashMap<String, Component> components = new HashMap<>();
    private HashMap<String, Set<String>> componentChanges = new HashMap<>();

    public void loadDependencies(String logFile) throws Exception {
        System.out.println("Loading commit history");
        Stopwatch stopwatch = Stopwatch.createStarted();

        patternComponent = mode == Mode.FINE_GRAINED ? PATTERN__COMPONENT_FINE_GRAINED :
                PATTERN__COMPONENT_COARSE_GRAINED;

        components = new HashMap<>();
        componentChanges = new HashMap<>();

        List<String> log = Files.readAllLines(Paths.get(logFile));

        // map from a commit hash to the list of
        // change components
        HashMap<String, Set<String>> changeSet = new HashMap<>();

        int countCommit = 0;

        final Pattern commitPattern = Pattern.compile(PATTERN__COMMIT);
        final Pattern classPattern = Pattern.compile(patternComponent);
        String hash = "";

        for(String s : log) {
            Matcher m = commitPattern.matcher(s);
            if(m.matches()) {
                countCommit++;
                hash = m.group(1) + "::" + m.group(2);
                // just being defensive here. it is not expected
                // to find the same hash twice in the log file.
                if(!changeSet.containsKey(hash)) {
                    changeSet.put(hash, new HashSet<>());
                }
            }
            else {
                m = classPattern.matcher(s);
                if (m.matches()) {
                    changeSet.get(hash).add(mode == Mode.COARSE_GRAINED ? m.group(2) :
                            m.group(2) + "." + m.group(4));
                }
            }
        }
        stopwatch.stop();
        System.out.println(String.format(" total number of commits: %d" , countCommit));
        System.out.println(String.format(" Time elapsed: %d ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));

        buildDependencies(changeSet);
        pruneDependencies();
    }



    // make it package for test purpose only.
    void buildDependencies(HashMap<String, Set<String>> changeSet) {
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
        pp.exportComponentDependencies(components, minSupportCount);
    }

    public DependencyInfo getDependency(String c1, String c2) {
        if(components.containsKey(c1)) {
            return components.get(c1).getDependencyInfo(c2);
        }
        return null;
    }

    public Set<String> setOfCoupledComponents(String c) {
        return setOfCoupledComponents(c, minSupportCount);
    }

    public Set<String> setOfCoupledComponents(String c, int minSupportCount) {
        if(components.containsKey(c)) {
            return components.get(c).setOfCoupledComponents(minSupportCount);
        }
        return null;
    }

    public int numberOfCoupledComponents(String c){
        return numberOfCoupledComponents(c, minSupportCount);
    }

    public int numberOfCoupledComponents(String c, int minSupportCount) {
        if(components.containsKey(c)) {
            return components.get(c).numberOfCoupledComponents(minSupportCount);
        }
        return 0;
    }

    public int sumOfCoupling(String c) {
        return sumOfCoupling(c, minSupportCount);
    }

    public int sumOfCoupling(String c, int minSupportCount) {
        if(components.containsKey(c)) {
            components.get(c).sumOfCoupling(minSupportCount);
        }
        return 0;
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

