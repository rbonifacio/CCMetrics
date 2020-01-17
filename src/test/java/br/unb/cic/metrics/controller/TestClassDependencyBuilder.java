package br.unb.cic.metrics.controller;

import br.unb.cic.metrics.model.DependencyInfo;
import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TestClassDependencyBuilder {

    DependencyManager mgr = new DependencyManager();

    @Before
    public void setUp() {
        mgr = new DependencyManager();

        HashMap<String, Set<String>> changes = new HashMap<>();

        changes.put("t1", new HashSet<>(Arrays.asList(new String[]{"c1", "c2", "c3", "c5"})));
        changes.put("t2", new HashSet<>(Arrays.asList(new String[]{"c4"})));
        changes.put("t3", new HashSet<>(Arrays.asList(new String[]{"c1", "c5"})));
        changes.put("t4", new HashSet<>(Arrays.asList(new String[]{"c1", "c2", "c3", "c5"})));
        changes.put("t5", new HashSet<>(Arrays.asList(new String[]{"c1", "c2", "c4", "c5"})));
        changes.put("t6", new HashSet<>(Arrays.asList(new String[]{"c1", "c2", "c4", "c5"})));


        mgr.buildDependencies(changes);
    }

    @Test
    public void testBuildDependencies() {
        DependencyInfo d1 = new DependencyInfo("c1", "c2", 4, 0.8);
        Assert.assertEquals(d1, mgr.getDependency("c1", "c2"));
    }

    @Test
    public void testSetOfCoupledClasses() {
        Set<String> expected = new HashSet<>((Arrays.asList(new String[]{"c2", "c5"})));
        Assert.assertEquals(expected, mgr.setOfCoupledClasses("c1", 3));
    }

    @Test
    public void testNumberOfCoupledClasses() {
        Assert.assertEquals(2, mgr.numberOfCoupledClasses("c1", 3));
    }

    @Test
    public void testSumOfCoupling() {
        Assert.assertEquals(9, mgr.sumOfCoupling("c1", 3));
    }
}
