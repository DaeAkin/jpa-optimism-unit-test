package dev.donghyeon.jpaoptimism;

import dev.donghyeon.jpaoptimism.domain.Article;
import dev.donghyeon.jpaoptimism.domain.ArticleSaveService;
import dev.donghyeon.jpaoptimism.domain.ArticleUpdateService;
import dev.donghyeon.jpaoptimism.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.swing.text.html.HTMLDocument;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Slf4j
public class ArticleOptimismTest {

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ArticleSaveService articleSaveService;
    @Autowired
    ArticleUpdateService articleUpdateService;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager tm;

    private TransactionTemplate transaction;

    @BeforeEach
    void setUp() {
        transaction = new TransactionTemplate(tm);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Test
//    @RepeatedTest(500)
    @Transactional
    void 낙관적락_테스트() throws InterruptedException {
        //given
        final int numberOfThreads = 8;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        String content = "가나다라";
        String changeContent = "마바사";
        Article article = transaction.execute(status -> articleSaveService.saveArticle(content));
        AtomicInteger exceptionCount = new AtomicInteger(0);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(numberOfThreads);


        //when
        for (int i = 0; i < numberOfThreads; i++) {
            int finalI = i;
            CompletableFuture.runAsync(() -> {
                transaction.execute(status -> {
                    try {
//                        articleUpdateService.updateArticle(article.getId(), changeContent + UUID.randomUUID());
                        System.out.println(finalI+":번째");
                        Article updateArticle = articleRepository.findById(article.getId()).orElseThrow();
                        cyclicBarrier.await();
                        updateArticle.updateContent(changeContent + UUID.randomUUID());
                    } catch (ObjectOptimisticLockingFailureException | InterruptedException | BrokenBarrierException e) {
                        exceptionCount.getAndIncrement();
                    } finally {
                        latch.countDown();
                    }
                    return null;
                });
            }).exceptionally(throwable -> {
                exceptionCount.getAndIncrement();
                throwable.printStackTrace();
                return null;
            });
        }
        latch.await();

        //다른스레드의 남은 작업을 조금 기다려준다.
        Thread.sleep(500);
        Article result = articleRepository.findById(article.getId()).orElseThrow();

        //then
        assertEquals(exceptionCount.get(), numberOfThreads - 1);
        assertEquals(result.getVersion(), 1);


    }

    @Test
    void 스레드테스트() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        for (int i = 0; i < 10; i++) {

            executorService.execute(() -> {
                try {

                    throw new RuntimeException();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


        }
        Thread.sleep(2000);
    }


}
