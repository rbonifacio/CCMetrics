package br.unb.cic.metrics.model;

import java.util.Objects;

public class DependencyInfo {

    private String source;
    private String target;
    private Integer supportCount;
    private Double conffidence;

    public DependencyInfo(String source, String target, Integer supportCount, Double conffidence) {
        this.source = source;
        this.target = target;
        this.supportCount = supportCount;
        this.conffidence = conffidence;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public Integer getSupportCount() {
        return supportCount;
    }

    public Double getConffidence() {
        return conffidence;
    }

    public String toString() {
        return source + " " + target + " " + supportCount + " " + conffidence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyInfo that = (DependencyInfo) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(target, that.target) &&
                Objects.equals(supportCount, that.supportCount) &&
                Objects.equals(conffidence, that.conffidence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, supportCount, conffidence);
    }
}
