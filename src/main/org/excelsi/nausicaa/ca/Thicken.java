package org.excelsi.nausicaa.ca;


public abstract class Thicken extends TargetCellMutator {
    protected final boolean test(byte t) {
        return t==0 && chance();
    }
}
