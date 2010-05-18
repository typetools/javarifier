package javarifier;

import javarifier.util.Pair;
import java.util.List;
import java.util.ArrayList;

//variable is inferred as true as a result of constraint solving
public class VarEvidence {
    //could use hashset and map, but do we expect these to be very big?
    
    // a source cause (nonnull if it's directly implied by some element of the code)
    private SourceCause cause;

    // all single-guarded constraints (the consequent is implicit)
    private ArrayList<ConstraintVar> single;

    // all double-guarded constraints (the consequent is implicit)
    private ArrayList<Pair<ConstraintVar, ConstraintVar>> guarded;

    public VarEvidence() {
        single = new ArrayList<ConstraintVar>();
        guarded = new ArrayList<Pair<ConstraintVar, ConstraintVar>>();
    }

    public void add(ConstraintVar v) { if(!single.contains(v)) single.add(v); }
    public void add(Pair<ConstraintVar, ConstraintVar> v) { if(!guarded.contains(v)) guarded.add(v); }
    public void setCause(SourceCause x) { cause = x; }
    public SourceCause getCause() { return cause; }
    
    public List<ConstraintVar> getSingleGuards() {
        return new ArrayList<ConstraintVar>(single);
    }
    public List<Pair<ConstraintVar, ConstraintVar>> getDoubleGuards() {
        return new ArrayList<Pair<ConstraintVar,ConstraintVar>>(guarded);
    }
}
