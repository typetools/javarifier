package javarifier;

import javarifier.JrType.*;

import java.util.*;

import javarifier.util.Pair;

/**
 * A MethodSig represents the Javari types of a method signature.
 */
public class MethodSig extends Signature {

  /**
   * The parameters of the method.
   */
  private List<JrType> params;

  /**
   * The return type of the method.
   */
  private JrType returnType;

  /**
   * The type of the method's receiver.
   */
  private JrType receiverType;

  /**
   * Constructs a new MethodSig.
   *
   * @param typeParams - any type parameters on the method
   * @param receiverType - the type of the receiver
   * @param params - the types of the parameters
   * @param returnType - the type of the return value
   */
  public MethodSig(List<Pair<VarType, JrType>> typeParams,
      JrType receiverType,
      List<JrType> params,
      JrType returnType) {
    super(typeParams);
    this.receiverType = receiverType;
    this.params = params;
    this.returnType = returnType;
    checkRep();
  }

  /**
   * Checks that this has all its information.
   */
  private void checkRep() {
    if (this.getTypeParams() == null) {
      throw new RuntimeException();
    }
    if (this.receiverType == null) {
      throw new RuntimeException();
    }
    if (this.params == null) {
      throw new RuntimeException();
    }
    if (this.returnType == null) {
      throw new RuntimeException();
    }
  }

  /**
   * Returns a list of all the types of the parameters.
   */
  public List<JrType> getParams() {
    return params;
  }

  /**
   * Returns the type of the method's return type.
   */
  public JrType getReturnType() {
    return returnType;
  }

  /**
   * Returns the type of the receiver.
   */
  public JrType getReceiverType() {
    return receiverType;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    return super.toString() + " " + returnType + " " + params +
    " " + receiverType;
  }

}
