import java.util.Vector;

public class VectorCopyInto {

  public String[] getMessageChain2(Vector<String> v)
  {
    String[] chain = new String[v.size()];
    v.copyInto (chain);
    return chain;
  }

}
