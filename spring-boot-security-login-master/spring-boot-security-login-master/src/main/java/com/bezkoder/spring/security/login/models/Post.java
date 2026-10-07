package com.bezkoder.spring.security.login.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String category = "announcement";

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private Integer clapCount = 0;

    @Column(nullable = false)
    private Integer celebrateCount = 0;

    @Column(nullable = false)
    private Integer laughCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Reply> replies = new ArrayList<>();

    public Post() {
    }

    public Post(String author, String title, String content, String category) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getClapCount() {
        return clapCount;
    }

    public void setClapCount(Integer clapCount) {
        this.clapCount = clapCount;
    }

    public Integer getCelebrateCount() {
        return celebrateCount;
    }

    public void setCelebrateCount(Integer celebrateCount) {
        this.celebrateCount = celebrateCount;
    }

    public Integer getLaughCount() {
        return laughCount;
    }

    public void setLaughCount(Integer laughCount) {
        this.laughCount = laughCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Reply> getReplies() {
        return replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }

    public void addReply(Reply reply) {
        this.replies.add(reply);
        reply.setPost(this);
    }

    public int getHotScore() {
        int replyScore = this.replies == null ? 0 : this.replies.size();
        return (likeCount == null ? 0 : likeCount)
                + (clapCount == null ? 0 : clapCount)
                + (celebrateCount == null ? 0 : celebrateCount)
                + (laughCount == null ? 0 : laughCount)
                + replyScore * 3;
    }
}
