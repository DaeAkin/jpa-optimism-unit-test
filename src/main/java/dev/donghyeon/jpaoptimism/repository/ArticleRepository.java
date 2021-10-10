package dev.donghyeon.jpaoptimism.repository;

import dev.donghyeon.jpaoptimism.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article,Long> {
}
