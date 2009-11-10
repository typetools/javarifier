public class CellClient3 {

    /*readonly*/ Cell</*this-mutable*/ Date> f;

      public void foo() /*mutable*/ {
       /*mutable*/ Date d = f.getVal();
        d.setDay(3);
    }

    public void bar() /*readonly*/ {
        /*readonly*/ Date rd = f.getVal();
        int x = rd.getDay();
    }

}
