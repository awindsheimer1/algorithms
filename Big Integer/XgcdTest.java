import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

// NOTE: BigInteger is used only for convenience in printing and converting byte[]'s
import java.math.BigInteger;

public class XgcdTest {
    private static boolean QUIET = false;

    public static void main(String[] args) {
        checkIfQuiet(args);
        printIfLoud("Enter each in decimal");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String one, two;
        try {
            printIfLoud("First operand (a):");
            one = input.readLine();
            printIfLoud("Second operand (b):");
            two = input.readLine();
        } catch (IOException e) {
            System.err.println("Failed to read input");
            return;
        }
        HeftyInteger hiOne = new HeftyInteger(new BigInteger(one).toByteArray());
        HeftyInteger hiTwo = new HeftyInteger(new BigInteger(two).toByteArray());
        HeftyInteger[] result = hiOne.XGCD(hiTwo);
        printIfLoud("Result, where a*x + b*y = GCD(x,y):");
        System.out.print("GCD(a, b) = ");
        printHeftyInteger(result[0]);
        System.out.print("x = ");
        printHeftyInteger(result[1]);
        System.out.print("y = ");
        printHeftyInteger(result[2]);
    }

    private static void printIfLoud(String s) {
        if (!QUIET) System.out.println(s);
    }

    private static void checkIfQuiet(String[] args) {
        if (args.length >= 1 && args[0].equals("-q")) QUIET = true;
    }

    public static void printHeftyInteger(HeftyInteger hi) {
        System.out.println(new BigInteger(hi.getVal()).toString());
    }

}

