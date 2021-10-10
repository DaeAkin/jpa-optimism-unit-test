package dev.donghyeon.jpaoptimism.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@ToString
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    private String content;

    public Article(String content) {
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}


