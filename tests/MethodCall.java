/**
 * This class tests non-parametric, non-static, intra-class method
 * calls.
 */
public class MethodCall {

    private /*this-mutable*/ MethodCall f;

    private /*readonly*/ MethodCall g;

    private /*romaybe*/ MethodCall getF() {
        return this.f;
    }

    private /*readonly*/ MethodCall getG() {
        return this.g;
    }

    private void setF(MethodCall mc) {
        this.f = mc;
    }

    private void setG(MethodCall mc) {
        this.g = mc;
    }


    private void foo() {
        /*mutable*/ MethodCall x = this.getF();
        x.setF(this);  // forces getFmut() to have mutable return type, and, therefore, mutable reciever.
    }

    private void bar() {
        /*readonly*/ MethodCall x = getF();
    }

    private void baz(MethodCall x) {
        this.setG(x);
    }

    private void quax(MethodCall mc) {
        this.setF(mc);
    }

    private void deadbeef() {
        /*readonly*/ MethodCall x = this.getG();
        x.bar();
        /*mutable*/ MethodCall y = this.getF();
        y.baz(x);
    }

}
