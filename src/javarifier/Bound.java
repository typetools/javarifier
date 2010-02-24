package javarifier;

/**
 * Enumerates the different types of bounds {@link #LOWER} and {@link #UPPER},
 * along with {@link #NONE} for situations where there are no bounds.
 */
public enum Bound {
    UPPER,
    LOWER,
    NONE
}
