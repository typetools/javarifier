public class Arrays {

    public void foo() /*readonly*/ {
        /*readonly*/Date[/*mutable*/] ma = new Date[1];
        /*readonly*/ Date d = new Date();
        ma[0] = d;
        /*readonly*/ Date x = ma[0];
        int y = x.getDay();
    }

    public void bar() /*readonly*/ {
        /*mutable*/Date[/*mutable*/] ma = new Date[1];
        /*mutable*/ Date d = new Date();
        ma[0] = d;
        /*mutable*/ Date x = ma[0];
        int y = x.getDay();
        x.setDay(y);
    }

    public void baz() /*readonly*/ {
        /*mutable*/Date[/*mutable*/] ma = new Date[1];
        ma[0] = new Date();
        ma[0].setDay(2);

        /*? readonly*/Date[/*readonly*/] ra = ma;
        /*readonly*/ Date x = ra[0];
        int y = x.getDay();

     }


    public void quax() /*readonly*/ {
        /*mutable*/Date[/*mutable*/] ma = new Date[1];
        /*mutable*/ Date d = new Date();
        ma[0] = d;

        /*mutable*/Date[/*readonly*/] ra = ma;
        /*mutable*/ Date x = ra[0];
        int y = x.getDay();
        x.setDay(y);
    }
}
