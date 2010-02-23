import java.util.Vector;

public class VectorCopyInto {

  public String[] getMessageChain2(Vector list)
  {
    String[] chain = new String[list.size()];
    list.copyInto (chain);
    return chain;
  }

}
