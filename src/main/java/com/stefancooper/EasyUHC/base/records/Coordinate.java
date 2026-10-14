package com.stefancooper.EasyUHC.base.records;

public record Coordinate(double x, double z) {

    @Override
    public String toString() { return Math.round(x()) + "," + Math.round(z()); }
}

