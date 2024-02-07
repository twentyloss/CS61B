/** Class that prints the Collatz sequence starting from a given number.
 *  @author YOUR NAME HERE
 */
public class Collatz {

    /** return the next number of n in Collatz sequence
        if n is odd, then 3*n + 1
        else return half of n
     */
    public static int nextNumber(int n) {
        if (n % 2 == 1){
            return 3 * n + 1;
        }
        else{
            return n / 2;
        }
    }

    public static void main(String[] args) {
        int n = 5;
        System.out.print(n + " ");
        while (n != 1) {
            n = nextNumber(n);
            System.out.print(n + " ");
        }
        System.out.println();
    }
}

