package com.gabigutu.threads;

import java.util.concurrent.locks.ReentrantLock;

class HelloThread extends Thread {

    private int myId;

    public HelloThread(int id) {
        this.myId = id;
    }

    public int getMyId() {
        return myId;
    }

    @Override
    public void run() {
        System.out.println("Hello from thread " + this.myId);
    }
}

class ComputingThread implements Runnable {

    private int start;
    private int end;

    public ComputingThread(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        // System.out.println("Thread computing from " + start + " to " + end);
        for (int i = start; i < end; i++) {
            Main.u[i] = Main.v[i] + 2;
        }
    }
}

class AddingThread extends Thread {

    private ReentrantLock reentrantLock;

    public AddingThread(ReentrantLock reentrantLock) {
        this.reentrantLock = reentrantLock;
    }

    @Override
    public void run() {

        for (int i = 0; i < 10000; i++) {
            // .. code
            this.reentrantLock.lock(); // busy waiting
            Main.sum = Main.sum + i; // race condition; operation is not atomic!!!
            this.reentrantLock.unlock();
            // .. code
        }

    }
}

public class Main {

    public static int v[], u[];
    public static int sum;

    private static void singleThreaded() {
        // single threaded
        for (int i = 0; i < 100; i++) {
            System.out.println(i);
        }
    }

    public static void main(String[] args) {

        // HelloThread helloThread = new HelloThread(4);
        // System.out.println("Thread name: " + helloThread.getName() + " and id: " +
        // helloThread.getMyId());
        // helloThread.start();

        HelloThread[] helloThreads = new HelloThread[10];
        for (int i = 0; i < 10; i++) {
            helloThreads[i] = new HelloThread(i + 1);
            helloThreads[i].start();
        }

        for (int i = 0; i < 10; i++) {
            try {
                helloThreads[i].join();
            } catch (InterruptedException exception) {
                System.err.println(exception.getMessage());
                exception.printStackTrace();
            }
        }

        System.out.println("Hello from thread main after all threads started!");

        int size = 1000 * 1000 * 10;
        v = new int[size];
        u = new int[size];
        for (int i = 0; i < size; i++) {
            v[i] = i + 1;
        }

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            u[i] = v[i] + 2;
        }
        long endTime = System.currentTimeMillis();
        long milliseconds = endTime - startTime;

        System.out.println("(single threaded) Time spent: " + milliseconds + " ms");

        int noThreads = 8;
        Thread[] computingThreads = new Thread[noThreads];
        int chunk = size / noThreads;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < noThreads; i++) {
            computingThreads[i] = new Thread(new ComputingThread(i * chunk, (i + 1) * chunk));
            computingThreads[i].start();
        }
        for (int i = 0; i < noThreads; i++) {
            try {
                computingThreads[i].join();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
        endTime = System.currentTimeMillis();
        milliseconds = endTime - startTime;

        System.out.println("(multi threaded) Time spent: " + milliseconds + " ms");

        ReentrantLock reentrantLock = new ReentrantLock();
        AddingThread[] addingThreads = new AddingThread[10];
        for (int i = 0; i < 10; i++) {
            addingThreads[i] = new AddingThread(reentrantLock);
            addingThreads[i].start();
        }
        for (int i = 0; i < 10; i++) {
            try {
                addingThreads[i].join();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
        System.out.println("Sum: " + Main.sum);

    }
}
