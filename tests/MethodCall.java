/**
 * This class tests non-parametric, non-static, intra-class method
 * calls.
 */
public class MethodCall {

    private /*this-mutable*/ MethodCall f;

    private /*readonly*/ MethodCall g;

    private /*romaybe*/ MethodCall getF() /*romaybe*/ {
        return this.f;
    }

    private /*readonly*/ MethodCall getG() /*readonly*/ {
        return this.g;
    }

    private void setF(MethodCall mc) /*mutable*/ {
        this.f = mc;
    }

    private void setG(MethodCall mc) /*mutable*/ {
        this.g = mc;
    }


    private void foo() /*mutable*/ {
        /*mutable*/ MethodCall x = this.getF();
        x.setF(this);  // forces getFmut() to have mutable return type, and, therefore, mutable reciever.
    }

    private void bar() /*readonly*/ {
        /*readonly*/ MethodCall x = getF();
    }

    private void baz(MethodCall x) /*mutable*/ {
        this.setG(x);
    }

    private void quax(MethodCall mc) /*mutable*/ {
        this.setF(mc);
    }

    private void deadbeef() /*mutable*/ {
        /*readonly*/ MethodCall x = this.getG();
        x.bar();
        /*mutable*/ MethodCall y = this.getF();
        y.baz(x);
    }

}
