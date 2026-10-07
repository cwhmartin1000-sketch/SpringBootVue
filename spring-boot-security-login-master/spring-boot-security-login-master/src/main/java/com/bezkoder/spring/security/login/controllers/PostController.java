package com.bezkoder.spring.security.login.controllers;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.spring.security.login.models.Post;
import com.bezkoder.spring.security.login.models.Reply;
import com.bezkoder.spring.security.login.payload.request.PostRequest;
import com.bezkoder.spring.security.login.payload.request.ReplyRequest;
import com.bezkoder.spring.security.login.repository.PostRepository;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/posts")
    public List<Post> getPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/posts/hot")
    public List<Post> getHotPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .sorted(Comparator.comparingInt(Post::getHotScore).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> createPost(@Valid @RequestBody PostRequest request, Principal principal) {
        String author = principal != null ? principal.getName() : "匿名同事";
        Post post = new Post();
        post.setAuthor(author);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory() == null || request.getCategory().isBlank() ? "announcement"
                : request.getCategory());
        return ResponseEntity.status(HttpStatus.CREATED).body(postRepository.save(post));
    }

    /**
     * 刪除目前登入使用者自己建立的貼文。
     *
     * @param postId    貼文識別碼
     * @param principal 目前登入使用者
     * @return 刪除結果
     */
    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, Principal principal) {
        return postRepository.findById(postId)
                .map(post -> {
                    if (principal == null || !post.getAuthor().equals(principal.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }

                    postRepository.delete(post);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/posts/{postId}/replies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> addReply(@PathVariable Long postId, @Valid @RequestBody ReplyRequest request,
            Principal principal) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("貼文不存在"));

        Reply reply = new Reply();
        reply.setAuthor(principal != null ? principal.getName() : "匿名同事");
        reply.setContent(request.getContent());
        post.addReply(reply);

        return ResponseEntity.ok(postRepository.save(post));
    }

    @PutMapping("/posts/{postId}/reaction")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Post> updateReaction(@PathVariable Long postId, @RequestBody Map<String, String> payload) {
        String reactionKey = payload.get("reactionKey");
        if (reactionKey == null || reactionKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("貼文不存在"));

        switch (reactionKey) {
            case "like":
                post.setLikeCount(post.getLikeCount() + 1);
                break;
            case "clap":
                post.setClapCount(post.getClapCount() + 1);
                break;
            case "celebrate":
                post.setCelebrateCount(post.getCelebrateCount() + 1);
                break;
            case "laugh":
                post.setLaughCount(post.getLaughCount() + 1);
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(postRepository.save(post));
    }
}
