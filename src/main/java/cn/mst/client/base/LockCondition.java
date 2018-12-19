package cn.mst.client.base;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName LockCondition
 * @Author buchengyin
 * @Date 2018/12/19 16:42
 **/
public class LockCondition {
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void await(){
        lock.lock();
        try{
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }finally {
            lock.unlock();
        }
    }

    public void await(long time){
        lock.lock();
        try{
            condition.await(time, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void single(){
        lock.lock();
        try {
            condition.signal();
        }finally {
            lock.unlock();
        }
    }
}
