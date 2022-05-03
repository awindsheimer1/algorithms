# CS/COE 1501 Assignment 5

## Goal:

To get hands on experience with algorithms to perform mathematical operations on large integers.

## High-level description:
You will be writing a replacement for Java's `BigInteger` to perform multiplications and to run the extended Euclidean algorithm on integers values that would overflow `long`.

## Specifications:
1.  You are provided with the start of a class to process arbitrarily sized integers called `HeftyInteger`. `HeftyInteger`s are represented internally as [two's-complement](https://en.wikipedia.org/wiki/Two%27s_complement) _raw integers_ using byte arrays (i.e., instances of `byte[]`).
	1.  Currently, `HeftyInteger` has the following operations implemented:
		*  A constructor that creates a new `HeftyInteger` based off of a provided `byte[]`.
		*  A method to compute the sum of two `HeftyInteger`s.
		*  A method to determine the negation of a `HeftyInteger`.
		*  A method to compute the difference of two `HeftyInteger`s.
		*  Several other helper methods.
	1.  Due to the use of a two's complement representation of the integers, positive `HeftyInteger`s should always have at least one leading 0 bit (indicating that the integer is positive) in their `byte[]` representation. This property may cause the array to be bigger than expected (e.g., a 1024-bit positive integer will be represented using a length 129 byte array).
	1.  `HeftyInteger`s are also be represented using a _big-endian_ byte-order, so the most significant byte is at the 0<sup>th</sup> index of the `byte[]`.
	1.  You will need to implement the following functions:
		*  `HeftyInteger multiply(HeftyInteger other)`
		*  `HeftyInteger[] XGCD(HeftyInteger other)`
		*  Any additional helper functions that you deem necessary.
	1.  You may *not* use any calls the Java API class `java.math.BigInteger`, or any other JCL class within `HeftyInteger`.
1. Once `HeftyInteger` is complete, make sure your implementation of `HeftyInteger` can be used to run the driver programs contained in `MultiplicationTest.java` and `XgcdTest.java`. To get full credit, your implementation should be efficient enough to complete multiplication or XGCD given 200-digit inputs within 3 minutes.

## Submission Guidelines:
*  **DO NOT SUBMIT** any IDE package files.
* You must be able to compile the driver programs by running `javac MultiplicationTest.java` and `javac XgcdTest.java`, respectively.
* You must be able to run the driver program by running `java MultiplicationTest` and `java XgcdTest`, respectively.
*  You must fill out `info_sheet.txt`.
*  Be sure to remember to push the latest copy of your code back to your GitHub repository before the the assignment is due.  At the deadline, the repositories will automatically be copied for grading.  Whatever is present in your GitHub repository at that time will be considered your submission for this assignment.

## Grading Rubric
*  `HeftyInteger`
	*  `multiply` works properly:  40
	*  `XGCD` works properly:  55
*  Assignment info sheet/submission:  5
