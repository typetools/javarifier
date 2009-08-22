package javarifier;

/**
 * The context of a local variable.  Local variables of polyread
 * methods have two contexts: the one where the method return is
 * mutable and one where the return type is readonly.  The "NONE"
 * context is for use with fields that do not have contexts.
 */
public enum Context {
    READONLY,
    MUTABLE,
    NONE
}
