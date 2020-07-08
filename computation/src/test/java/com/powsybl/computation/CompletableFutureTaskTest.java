package com.powsybl.computation;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
@RunWith(Parameterized.class)
public class CompletableFutureTaskTest {

    private final Executor executor;

    public CompletableFutureTaskTest(Executor executor) {
        this.executor = executor;
    }

    /**
     * Check the behaviour is the same with different types of executors.
     * In particular, {@link ThreadPoolExecutor} and {@link ForkJoinPool}
     * have some behaviour discrepancies, which should be hidden by our implementation.
     */
    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return executors().stream()
                .map(e -> new Object[] {e})
                .collect(Collectors.toList());
    }

    private static List<Executor> executors() {
        return ImmutableList.of(
                Executors.newSingleThreadExecutor(),
                Executors.newCachedThreadPool(),
                Executors.newWorkStealingPool(),
                ForkJoinPool.commonPool()
        );
    }

    @Test
    public void whenSupplyObjectThenReturnIt() throws Exception {

        Object res = new Object();
        CompletableFutureTask<Object> task = CompletableFutureTask.runAsync(() -> res, executor);
        assertSame(res, task.get());
    }

    private static class MyException extends RuntimeException {
    }

    @Test
    public void whenTaskThrowsThenThrowExecutionException() {

        CompletableFutureTask<Integer> task = CompletableFutureTask.runAsync(() -> {
            throw new MyException();
        }, executor);

        try {
            task.get();
            fail();
        } catch (ExecutionException exc) {
            assertTrue(exc.getCause() instanceof MyException);
        } catch (Throwable exc) {
            fail();
        }
    }

    @Test(expected = CancellationException.class)
    public void whenCancelBeforeExecutionThenThrowAndDontExecute() throws Exception {

        CompletableFutureTask<Integer> task = new CompletableFutureTask<>(() -> {
            fail();
            return null;
        });
        boolean cancelled = task.cancel(true);
        assertTrue(cancelled);
        task.runAsync(executor);
        task.get();
    }

    @Test
    public void whenCancelDuringExecutionThenThrowAndInterrupt() throws Exception {

        CountDownLatch waitForStart = new CountDownLatch(1);
        CountDownLatch waitIndefinitely = new CountDownLatch(1);
        CountDownLatch waitForInterruption = new CountDownLatch(1);

        AtomicBoolean interrupted = new AtomicBoolean(false);
        CompletableFutureTask<Integer> task = CompletableFutureTask.runAsync(() -> {
            waitForStart.countDown();
            try {
                waitIndefinitely.await();
                fail();
            } catch (InterruptedException exc) {
                interrupted.set(true);
                waitForInterruption.countDown();
            }
            return null;
        }, executor);

        //Cancel after task has actually started
        waitForStart.await();
        boolean cancelled = task.cancel(true);
        assertTrue(cancelled);

        try {
            task.get();
            fail("Should not happen: task has been cancelled");
        } catch (CancellationException exc) {
            //ignored
        }

        waitForInterruption.await();
        assertTrue(interrupted.get());

        //Second call to cancel should return false
        cancelled = task.cancel(true);
        assertFalse(cancelled);
    }

    @Test
    public void cancelAfterExecutionShouldDoNothing() throws Exception {

        Object res = new Object();
        CompletableFutureTask<Object> task = CompletableFutureTask.runAsync(() -> res, executor);

        assertSame(res, task.get());

        boolean cancelled = task.cancel(true);
        assertFalse(cancelled);
        assertSame(res, task.get());
    }

}
