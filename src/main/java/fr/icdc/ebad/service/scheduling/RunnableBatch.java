package fr.icdc.ebad.service.scheduling;

import java.util.Date;

public class RunnableBatch implements Runnable {
    private final String message;

    public RunnableBatch(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        System.out.println(new Date() + "STARRRT Runnable Task with " + message + " on thread " + Thread.currentThread().getName());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(new Date() + "ENDDDD Runnable Task with " + message + " on thread " + Thread.currentThread().getName());
    }
}
