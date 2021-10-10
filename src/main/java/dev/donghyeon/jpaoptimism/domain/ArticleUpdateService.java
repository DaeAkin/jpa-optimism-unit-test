package dev.donghyeon.jpaoptimism.domain;

import dev.donghyeon.jpaoptimism.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleUpdateService {

    private final ArticleRepository articleRepository;

    @Transactional
    public void updateArticle(Long articleId, String content) {
        log.debug("updateArticle called : {} ",content);
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.updateContent(content);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateByArticle(Article article, String content) {
        log.debug("updateByArticle called : {} ",content);
        article.updateContent(content);
    }
}
