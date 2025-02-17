package br.unb.cic.metrics.model;

import java.util.*;
import java.util.stream.Collectors;

public class Component {

    class Dependency {
        String target;
        Integer supportCount;

        public Dependency(String target) {
            this.target = target;
            supportCount = 1;
        }

        public String export(Integer changes) {
            return target + "; " + supportCount + "; " + (float)supportCount / changes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Dependency that = (Dependency) o;
            return Objects.equals(target, that.target) &&
                    Objects.equals(supportCount, that.supportCount);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, supportCount);
        }

        public boolean isValid(Integer changes, int minSupportCount, double minConfidence) {
            return supportCount >= minSupportCount &&
                    ((float)supportCount / changes) >= minConfidence;
        }
    }

    private String name;
    private HashMap<String, Dependency> dependencies;
    private Integer changes;

    public Component(String name) {
        this.name = name;
        this.changes = 0;
        this.dependencies = new HashMap<>();
    }

    public Object getName() {
        return name;
    }

    public void reportChange() {
        changes++;
    }

    public List<DependencyInfo> listDependencyInfo() {
        List<DependencyInfo> res = new ArrayList<>();
        dependencies.values().forEach(d -> res.add(getDependencyInfo(d.target)));
        return res;
    }

    public List<String> listDependencies() {
        List<String> res = new ArrayList<>();
        dependencies.keySet().forEach(k -> res.add(String.format("%s; %s", name, dependencies.get(k).export(changes))));
        return res;
    }

    public void addCoChangeDependecy(String target) {
        Dependency dep = new Dependency(target);

        if(dependencies.containsKey(target)) {
            dep.supportCount = dependencies.get(target).supportCount + 1;
        }
        dependencies.put(target, dep);
    }

    public void pruneDependencies(int minSupportCount, double minConfidence) {
        Set<String> toRemove = new HashSet<>();
        dependencies.keySet().forEach(k -> {
            if(!dependencies.get(k).isValid(changes, minSupportCount, minConfidence)) {
                toRemove.add(k);
            }
        });

        toRemove.forEach(k -> dependencies.remove(k));
    }

    public DependencyInfo getDependencyInfo(String target) {
        if(dependencies.containsKey(target)) {
            Dependency d = dependencies.get(target);

            return new DependencyInfo(name, d.target, d.supportCount, (double)d.supportCount/changes);
        }
        return null;
    }


    public Set<String> setOfCoupledComponents(int minSupportCount) {
        return listDependencyInfo().stream()
                .filter(d -> d.getSupportCount() >= minSupportCount)
                .map(f -> f.getTarget())
                .collect(Collectors.toSet());
    }

    public int numberOfCoupledComponents(int minSupportCount) {
        List<String> classes = listDependencies();
        if(classes != null) {
            return classes.size();
        }
        return 0;
    }

    public int sumOfCoupling(int minSupportCount) {
        List<DependencyInfo> deps = listDependencyInfo();

        return deps.stream()
                .filter(d -> d.getSupportCount() >= minSupportCount)
                .map(f -> f.getSupportCount())
                .reduce(0, Integer::sum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Component component = (Component) o;
        return Objects.equals(name, component.name) &&
                Objects.equals(dependencies, component.dependencies) &&
                Objects.equals(changes, component.changes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dependencies, changes);
    }
}
