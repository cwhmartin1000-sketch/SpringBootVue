<template>
  <div class="board-page">
    <section class="wall-shell">
      <header class="wall-header">
        <div>
          <span class="eyebrow">同事互動牆</span>
          <h1>留言版</h1>
        </div>
        <div class="header-meta">
          <span class="live-pill">線上</span>
        </div>
      </header>

      <div class="composer">
        <input v-model="newTitle" class="title-input" type="text" maxlength="60" placeholder="輸入標題..." />
        <textarea v-model="newContent" rows="3" placeholder="分享最新狀態、想法或需要大家協助的事項..." />
        <div class="composer-tools">
          <select v-model="newCategory">
            <option value="announcement">公告</option>
            <option value="team">團隊</option>
            <option value="idea">想法</option>
            <option value="support">協助</option>
          </select>
          <button class="publish-btn" @click="submitPost">發布貼文</button>
        </div>
      </div>

      <div v-if="hotPosts.length" class="hot-posts">
        <div class="section-title">熱門留言</div>
        <div class="hot-list">
          <div v-for="post in hotPosts" :key="post.id" class="hot-item">
            <span class="hot-rank">#{{ post.rank }}</span>
            <div>
              <strong>{{ post.title }}</strong>
              <small>{{ post.author }} · {{ formatDate(post.createdAt) }}</small>
            </div>
          </div>
        </div>
      </div>

      <div class="feed">
        <article v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-head">
            <div class="avatar">{{ initials(post.author) }}</div>
            <div>
              <strong>{{ post.author }}</strong>
              <small>{{ formatDate(post.createdAt) }}</small>
            </div>
            <button
              v-if="currentUser && currentUser.username === post.author"
              class="delete-post-btn"
              type="button"
              @click="deletePost(post)"
            >刪除貼文</button>
          </div>

          <div class="post-content">
            <span class="category-badge">{{ post.category }}</span>
            <h3>{{ post.title }}</h3>
            <p>{{ post.content }}</p>
          </div>

          <div class="reaction-row">
            <button
              v-for="reaction in reactionList"
              :key="reaction.key"
              @click="toggleReaction(post.id, reaction.key)"
              class="reaction-btn"
              :class="{ active: post.userReaction === reaction.key }"
            >
              <span>{{ reaction.icon }}</span>
              <span>{{ post[reaction.countKey] || 0 }}</span>
            </button>
          </div>

          <div class="reply-list">
            <div v-for="reply in post.replies || []" :key="reply.id" class="reply-item">
              <span class="reply-author">{{ reply.author }}</span>
              <span>{{ reply.content }}</span>
            </div>
          </div>

          <div class="reply-box">
            <input
              v-model="replyDrafts[post.id]"
              type="text"
              :placeholder="'回覆 ' + post.author + '... '"
            />
            <button @click="submitReply(post.id)">回覆</button>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script>
import boardService from "../services/board.service";

export default {
  name: "BoardUser",
  data() {
    return {
      posts: [],
      hotPosts: [],
      newTitle: "",
      newContent: "",
      newCategory: "announcement",
      replyDrafts: {},
      reactionList: [
        { key: "like", icon: "👍", countKey: "likeCount" },
        { key: "clap", icon: "👏", countKey: "clapCount" },
        { key: "celebrate", icon: "🎉", countKey: "celebrateCount" },
        { key: "laugh", icon: "😂", countKey: "laughCount" }
      ]
    };
  },
  computed: {
    currentUser() {
      return this.$store.state.auth.user;
    }
  },
  mounted() {
    this.fetchPosts();
    this.fetchHotPosts();
  },
  methods: {
    normalizePost(post) {
      return {
        ...post,
        replies: post.replies || [],
        userReaction: null,
        likeCount: post.likeCount || 0,
        clapCount: post.clapCount || 0,
        celebrateCount: post.celebrateCount || 0,
        laughCount: post.laughCount || 0
      };
    },
    async fetchPosts() {
      try {
        const response = await boardService.getPosts();
        this.posts = (response.data || []).map(this.normalizePost);
      } catch (error) {
        console.error("載入貼文失敗", error);
      }
    },
    async fetchHotPosts() {
      try {
        const response = await boardService.getHotPosts();
        this.hotPosts = (response.data || []).map((post, index) => ({
          ...this.normalizePost(post),
          rank: index + 1
        }));
      } catch (error) {
        console.error("載入熱門留言失敗", error);
      }
    },
    initials(name) {
      if (!name) return "同";
      return name
        .split(" ")
        .map((part) => part.charAt(0).toUpperCase())
        .slice(0, 2)
        .join("");
    },
    formatDate(value) {
      if (!value) return "剛剛";
      const date = new Date(value);
      return date.toLocaleString("zh-TW", {
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
      });
    },
    async submitPost() {
      if (!this.currentUser) {
        this.$router.push("/login");
        return;
      }

      if (!this.newTitle.trim() || !this.newContent.trim()) {
        return;
      }

      try {
        await boardService.createPost({
          title: this.newTitle.trim(),
          content: this.newContent.trim(),
          category: this.newCategory || "announcement"
        });

        this.newTitle = "";
        this.newContent = "";
        this.newCategory = "announcement";
        await this.fetchPosts();
        await this.fetchHotPosts();
      } catch (error) {
        console.error("發布貼文失敗", error);
      }
    },
    async submitReply(postId) {
      const content = (this.replyDrafts[postId] || "").trim();
      if (!content) {
        return;
      }

      try {
        await boardService.addReply(postId, { content });
        this.replyDrafts[postId] = "";
        await this.fetchPosts();
        await this.fetchHotPosts();
      } catch (error) {
        console.error("回覆失敗", error);
      }
    },
    async deletePost(post) {
      if (!window.confirm("確定要刪除這則貼文及其所有回覆嗎？此操作無法復原。")) {
        return;
      }

      try {
        await boardService.deletePost(post.id);
        await this.fetchPosts();
        await this.fetchHotPosts();
      } catch (error) {
        console.error("刪除貼文失敗", error);
      }
    },
    async toggleReaction(postId, reactionKey) {
      if (!this.currentUser) {
        this.$router.push("/login");
        return;
      }

      try {
        await boardService.toggleReaction(postId, reactionKey);
        await this.fetchPosts();
        await this.fetchHotPosts();
      } catch (error) {
        console.error("反應失敗", error);
      }
    }
  }
};
</script>

<style scoped>
.board-page {
  display: flex;
  justify-content: center;
  padding-top: 0.5rem;
}

.wall-shell {
  width: min(100%, 980px);
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 28px;
  padding: 1.5rem;
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

.wall-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.1rem;
}

.eyebrow {
  display: block;
  font-size: 0.72rem;
  letter-spacing: 0.12em;
  font-weight: 700;
  color: #3b82f6;
  text-transform: uppercase;
}

.wall-header h1 {
  margin: 0.4rem 0 0;
  font-size: clamp(2rem, 2.8vw, 3rem);
  line-height: 1.1;
  color: #0f172a;
}

.live-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  background: rgba(16, 185, 129, 0.12);
  color: #047857;
  padding: 0.5rem 0.8rem;
  border-radius: 999px;
  font-weight: 700;
}

.live-pill::before {
  content: "";
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  display: inline-block;
}

.composer {
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 20px;
  padding: 1rem;
  margin-bottom: 1.25rem;
}

.title-input,
.composer textarea,
.reply-box input,
.composer select {
  width: 100%;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 12px;
  padding: 0.8rem 0.9rem;
  background: #fff;
  color: #0f172a;
}

.composer textarea {
  margin-top: 0.8rem;
  resize: vertical;
  min-height: 110px;
}

.composer-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-top: 0.9rem;
}

.composer-tools select {
  width: 180px;
}

.publish-btn,
.reply-box button {
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #4f46e5, #3b82f6);
  color: #fff;
  font-weight: 700;
  padding: 0.8rem 1.2rem;
}

.hot-posts {
  margin-bottom: 1.3rem;
}

.section-title {
  font-size: 0.8rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: #64748b;
  text-transform: uppercase;
  margin-bottom: 0.7rem;
}

.hot-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.8rem;
}

.hot-item {
  display: flex;
  align-items: flex-start;
  gap: 0.7rem;
  padding: 0.9rem 1rem;
  border-radius: 16px;
  background: linear-gradient(135deg, #eef2ff, #f8fafc);
  border: 1px solid rgba(99, 102, 241, 0.12);
}

.hot-rank {
  font-size: 1.1rem;
  font-weight: 800;
  color: #4f46e5;
}

.hot-item strong,
.post-content h3 {
  display: block;
  color: #111827;
  font-weight: 700;
}

.hot-item small,
.post-head small {
  color: #64748b;
}

.feed {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.post-card {
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 22px;
  padding: 1rem 1.1rem;
}

.post-head {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  margin-bottom: 0.8rem;
}

.delete-post-btn {
  margin-left: auto;
  border: 1px solid #fecaca;
  border-radius: 10px;
  padding: 0.45rem 0.7rem;
  background: #fff;
  color: #b91c1c;
  font-size: 0.82rem;
}

.delete-post-btn:hover {
  background: #fef2f2;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.post-content {
  margin-bottom: 0.9rem;
}

.category-badge {
  display: inline-block;
  background: #e0e7ff;
  color: #3730a3;
  border-radius: 999px;
  padding: 0.32rem 0.7rem;
  font-size: 0.7rem;
  font-weight: 700;
  margin-bottom: 0.6rem;
}

.post-content h3 {
  font-size: 1.3rem;
  margin: 0 0 0.5rem;
}

.post-content p {
  margin: 0;
  line-height: 1.8;
  color: #334155;
}

.reaction-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  margin-bottom: 0.9rem;
}

.reaction-btn {
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: #f8fafc;
  color: #475569;
  border-radius: 999px;
  padding: 0.42rem 0.8rem;
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-weight: 600;
}

.reaction-btn.active {
  background: #e9d5ff;
  border-color: rgba(168, 85, 247, 0.2);
  color: #6d28d9;
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  margin-bottom: 0.8rem;
}

.reply-item {
  background: #f8fafc;
  border-radius: 12px;
  padding: 0.7rem 0.8rem;
  color: #334155;
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.reply-author {
  font-weight: 700;
  color: #0f172a;
  margin-right: 0.5rem;
}

.reply-box {
  display: flex;
  gap: 0.7rem;
}

.reply-box input {
  flex: 1;
  height: 42px;
}

@media (max-width: 768px) {
  .hot-list {
    grid-template-columns: 1fr;
  }

  .composer-tools,
  .reply-box,
  .wall-header {
    flex-direction: column;
    align-items: stretch;
  }

  .composer-tools select,
  .publish-btn,
  .reply-box button {
    width: 100%;
  }
}
</style>
