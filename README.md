# CCMetrics

This is a Java project that computes co-change data from
a fine-grained GIT repository (generated using the Kenja
tool https://github.com/niyaton/kenja).

## Build

To generate a JAR file with dependencies, execute the following
command.

```console
$ mvn clean compile assembly:single
```

This generates a file named CCMetrics-<version>.jar in the target
folder. Please, rename this file to CCMetrics.jar (just to simplify
the description of the usage scenario).

## Usage

First geneate a log file (from a fine-grained repository),
and then run the program using as command line arguments
the file log.txt and an output directory. See bellow how
to execute these commands. 

```console
$ git log --name-only --oneline --pretty='%ncommit: %H' > log.txt
$ java -jar CCMetrics <log.txt> <outdir> 
```

## Output

After running the CCMetrics program, two files will be generated in
the *outdir* folder. The first (change-history.csv), presents all commits
that change a every component (either coarse-grained or fine-grained).
The second (components.csv), presents the set of co-change dependencies
of all components (again, either coarse-grained or fine-grained).

That is it. 


