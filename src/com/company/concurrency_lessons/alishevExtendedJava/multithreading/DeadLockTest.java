package com.company.concurrency_lessons.alishevExtendedJava.multithreading;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.company.concurrency_lessons.vasko.ColorScheme.*;

public class DeadLockTest {
    public static void main(String[] args) throws InterruptedException {
        Runner runner = new Runner();

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                runner.firstThread();
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                runner.secondThread();
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        runner.finishThread();
    }
}

class Runner {
    private Account account1 = new Account();
    private Account account2 = new Account();
    private Lock lock1 = new ReentrantLock();
    private Lock lock2 = new ReentrantLock();

    private void takeLocks(Lock lock1, Lock lock2) {
        boolean firstLockTaken = false;
        boolean secondLockTaken = false;
        while (true) {
            try {
                firstLockTaken = lock1.tryLock();
                secondLockTaken = lock2.tryLock();
            } finally {
                if (firstLockTaken && secondLockTaken) {
                    return;
                }
                if (firstLockTaken) {
                    lock1.unlock();
                }
                if (secondLockTaken) {
                    lock2.unlock();
                }
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public void firstThread() {
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            takeLocks(lock1, lock2);
            try {
                Account.transfer(account1, account2, random.nextInt(100));
                if (i % 1000 == 0)
                    System.out.println(GREEN + "++");
            } finally {
                lock1.unlock();
                lock2.unlock();
            }

        }
    }

    public void secondThread() {
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            takeLocks(lock2, lock1);
            try {
                Account.transfer(account2, account1, random.nextInt(100));
                if (i % 1000 == 0)
                    System.out.println(RED + "++");
            } finally {
                lock1.unlock();
                lock2.unlock();
            }
        }
    }

    public void finishThread() {
        System.out.println(CYAN + "Account one is " + account1.getBalance());
        System.out.println(CYAN + "Account two is " + account2.getBalance());
        System.out.println(BLUE + "Total balance  " + (account1.getBalance() +
                account2.getBalance()));
    }
}

class Account {
    private int balance = 10000;
    // Lock lock = new ReentrantLock();

    public void deposit(int amount) {
        // lock.lock();
        balance += amount;
        // lock.unlock();
    }

    public void withdraw(int amount) {
        // lock.lock();
        balance -= amount;
        //lock.unlock();
    }

    public int getBalance() {
        return balance;
    }

    public static void transfer(Account acc1, Account acc2, int amount) {
        acc1.withdraw(amount);
        acc2.deposit(amount);
    }
}
