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
public class ArticleSaveService {

    private final ArticleRepository articleRepository;

    @Transactional
    public Article saveArticle(String content) {
        log.debug("saveArticle called : {} ",content);
        Article article = new Article(content);
        return articleRepository.save(article);
    }
}
