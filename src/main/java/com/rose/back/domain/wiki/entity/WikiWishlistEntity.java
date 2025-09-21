package com.rose.back.domain.wiki.entity;

import com.rose.back.domain.user.entity.UserEntity;
import com.rose.back.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rose_wiki_wishlist", 
      uniqueConstraints = {@UniqueConstraint(columnNames = {"user_no", "wiki_id"})})
public class WikiWishlistEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wiki_id", nullable = false)
    private WikiEntity wiki;
}