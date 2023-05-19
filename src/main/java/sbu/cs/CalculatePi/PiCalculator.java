package sbu.cs.CalculatePi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PiCalculator {
    private BigDecimal sum;
    private final Object lock = new Object();

    public String calculate(int floatingPoint) {
        sum = BigDecimal.ZERO;
        int numberOfThreads = 10;
        long range = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            long start = i * range;
            long finish = (i + 1) * range;
            executor.execute(new CalculatePi(start, finish));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sum = sum.multiply(BigDecimal.valueOf(4));
        sum = sum.setScale(floatingPoint, RoundingMode.DOWN);
        return sum.toString();
    }

    public static void main(String[] args) {
        PiCalculator calculator = new PiCalculator();
        int floatingPoint = 100; // Set the desired number of digits after the decimal point
        String pi = calculator.calculate(floatingPoint);
        System.out.println(pi);
    }

    private class CalculatePi implements Runnable {
        private final long start;
        private final long finish;

        public CalculatePi(long start, long finish) {
            this.start = start;
            this.finish = finish;
        }

        @Override
        public void run() {
            BigDecimal localSum = BigDecimal.ZERO;

            for (long i = start; i < finish; i++) {
                BigDecimal firstTerm = BigDecimal.ZERO;
                BigDecimal secondTerm = BigDecimal.ZERO;
                BigDecimal one = BigDecimal.ONE;

                BigDecimal first = BigDecimal.valueOf(5).pow((int) (2 * i + 1));
                BigDecimal denom = BigDecimal.valueOf((2 * i + 1) * Math.pow(-1, i));
                BigDecimal firstCoefficient = denom;
                first = first.multiply(firstCoefficient);

                firstTerm = one.divide(first, 10000, RoundingMode.HALF_EVEN);
                firstTerm = firstTerm.multiply(BigDecimal.valueOf(4));

                BigDecimal second = BigDecimal.valueOf(239).pow((int) (2 * i + 1));
                BigDecimal secondCoefficient = denom;
                second = second.multiply(secondCoefficient);

                secondTerm = BigDecimal.ONE.divide(second, 10000, RoundingMode.HALF_EVEN);

                BigDecimal finalTerm = firstTerm.subtract(secondTerm);

                localSum = localSum.add(finalTerm);
            }

            synchronized (lock) {
                sum = sum.add(localSum);
            }
        }
    }
}
