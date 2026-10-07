<template>
  <div class="home-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <span class="eyebrow">同事圈・團隊交流平台</span>
        <h1>讓工作上的大小事，都有地方分享。</h1>
        <p>在同一個留言版發布公告、交流想法、回覆同事，並用表情反應為彼此加油。</p>
        <div class="hero-actions">
          <router-link v-if="currentUser" to="/user" class="btn btn-primary primary-btn">前往留言版</router-link>
          <router-link v-else to="/login" class="btn btn-primary primary-btn">登入後進入留言版</router-link>
          <router-link v-if="!currentUser" to="/register" class="btn btn-outline-primary secondary-btn">建立帳號</router-link>
        </div>
      </div>
      <div class="home-illustration" aria-hidden="true">
        <div class="illustration-bubble bubble-one">公告</div>
        <div class="illustration-bubble bubble-two">💬</div>
        <div class="illustration-bubble bubble-three">🎉</div>
        <div class="illustration-orbit"></div>
      </div>
    </section>

    <section class="feature-grid">
      <article class="feature-card">
        <div class="icon-wrap purple"><font-awesome-icon icon="comment" /></div>
        <h3>分享消息</h3>
        <p>發布團隊公告、工作近況或需要協助的事項。</p>
      </article>
      <article class="feature-card">
        <div class="icon-wrap blue"><font-awesome-icon icon="smile" /></div>
        <h3>一起交流</h3>
        <p>回覆貼文、交換想法，讓討論集中又好找。</p>
      </article>
      <article class="feature-card">
        <div class="icon-wrap green"><font-awesome-icon icon="heart" /></div>
        <h3>給彼此鼓勵</h3>
        <p>用表情反應回應同事，讓每個好消息都被看見。</p>
      </article>
    </section>
  </div>
</template>

<script>
export default {
  name: "Home",
  computed: {
    currentUser() {
      return this.$store.state.auth.user;
    }
  }
};
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.hero-panel {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 2rem;
  align-items: center;
  background: linear-gradient(135deg, #111827, #1d4ed8);
  border-radius: 32px;
  padding: 2.5rem;
  box-shadow: 0 25px 60px rgba(30, 64, 175, 0.22);
  overflow: hidden;
  position: relative;
}

.hero-panel::before {
  content: "";
  position: absolute;
  inset: auto -40px -60px auto;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.hero-copy, .hero-visual, .home-illustration {
  position: relative;
  z-index: 1;
}

.home-illustration {
  display: grid;
  place-items: center;
  min-height: 250px;
}

.illustration-orbit {
  width: 210px;
  height: 210px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.18), rgba(255, 255, 255, 0.02) 68%);
}

.illustration-bubble {
  position: absolute;
  z-index: 1;
  display: grid;
  place-items: center;
  min-width: 74px;
  min-height: 54px;
  padding: 0.7rem 1rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  font-size: 1.2rem;
  font-weight: 700;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.18);
  backdrop-filter: blur(8px);
}

.bubble-one { transform: translate(-66px, -42px) rotate(-8deg); }
.bubble-two { transform: translate(72px, 0) rotate(8deg); }
.bubble-three { transform: translate(-28px, 68px) rotate(-4deg); }

.eyebrow {
  display: inline-block;
  font-size: 0.76rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(191, 219, 254, 0.9);
  font-weight: 700;
  margin-bottom: 1rem;
}

.hero-copy h1 {
  font-size: clamp(2.2rem, 4vw, 4rem);
  line-height: 1.06;
  letter-spacing: -0.05em;
  color: #fff;
  margin-bottom: 1rem;
  max-width: 560px;
}

.hero-copy p {
  color: rgba(255, 255, 255, 0.82);
  font-size: 1.05rem;
  max-width: 540px;
  margin-bottom: 1.75rem;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.85rem;
}

.primary-btn,
.secondary-btn {
  border-radius: 999px;
  padding: 0.8rem 1.4rem;
  font-weight: 700;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.primary-btn {
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  border: none;
  box-shadow: 0 18px 28px rgba(96, 92, 255, 0.38);
}

.secondary-btn {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.04);
}

.primary-btn:hover,
.secondary-btn:hover {
  transform: translateY(-1px);
}

.hero-visual {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 320px;
  position: relative;
}

.floating-card {
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(148, 163, 184, 0.25);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.2);
}

.card-main {
  width: min(100%, 320px);
  padding: 1.25rem 1.2rem;
  color: #eff6ff;
}

.mini-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: rgba(191, 219, 254, 0.9);
  margin-bottom: 1.2rem;
  font-size: 0.85rem;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #34d399;
  box-shadow: 0 0 12px rgba(52, 211, 153, 0.9);
}

.metric-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.1rem;
}

.metric-row small,
.card-accent small,
.card-accent .trend {
  color: rgba(191, 219, 254, 0.75);
}

.metric-row strong,
.card-accent strong {
  display: block;
  font-size: 2rem;
  line-height: 1;
  color: #fff;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
}

.pill.success {
  background: rgba(52, 211, 153, 0.15);
  color: #a7f3d0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 0.45rem;
  height: 110px;
  padding-top: 0.5rem;
}

.chart-bars span {
  display: block;
  flex: 1;
  border-radius: 999px 999px 0 0;
  background: linear-gradient(180deg, #a78bfa, #60a5fa);
}

.card-accent {
  position: absolute;
  right: -18px;
  bottom: 18px;
  padding: 1rem 1.1rem;
  color: #fff;
  min-width: 160px;
}

.card-accent strong {
  font-size: 1.7rem;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1.2rem;
}

.feature-card {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 24px;
  padding: 1.5rem;
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.06);
}

.icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  margin-bottom: 1rem;
  font-size: 1.2rem;
  color: #fff;
}

.icon-wrap.purple { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
.icon-wrap.blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.icon-wrap.green { background: linear-gradient(135deg, #10b981, #059669); }

.feature-card h3 {
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 0.7rem;
}

.feature-card p {
  margin: 0;
  color: #475569;
  line-height: 1.6;
}

.team-wall {
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.06);
  border-radius: 28px;
  padding: 1.5rem;
}

.wall-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
}

.wall-badge {
  display: inline-block;
  background: #eff6ff;
  border-radius: 999px;
  color: #2563eb;
  padding: 0.38rem 0.8rem;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.wall-header h2 {
  margin: 0.6rem 0 0;
  font-size: 2rem;
  font-weight: 800;
  color: #0f172a;
}

.wall-pill {
  border-radius: 999px;
  background: rgba(16, 185, 129, 0.12);
  color: #047857;
  font-weight: 700;
  border: 0;
}

.composer {
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 20px;
  padding: 1rem;
  margin-bottom: 1.25rem;
}

.composer textarea {
  width: 100%;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 16px;
  padding: 0.9rem 1rem;
  resize: vertical;
  min-height: 96px;
  background: #fff;
}

.composer textarea:focus,
.reply-form input:focus {
  outline: 2px solid rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.45);
}

.composer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.9rem;
  gap: 1rem;
}

.tag-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-picker span,
.post-tags span {
  display: inline-flex;
  align-items: center;
  background: #e0e7ff;
  color: #3730a3;
  border-radius: 999px;
  padding: 0.35rem 0.8rem;
  font-size: 0.72rem;
  font-weight: 600;
}

.publish-btn {
  border-radius: 999px;
  padding: 0.7rem 1.2rem;
  font-weight: 700;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.post-card {
  background: #ffffff;
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

.post-head strong {
  display: block;
  color: #111827;
}

.post-head small {
  color: #64748b;
}

.post-text {
  margin: 0 0 0.9rem;
  color: #1f2937;
  line-height: 1.7;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  margin-bottom: 0.9rem;
}

.reaction-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  margin-bottom: 0.8rem;
}

.reaction-btn {
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: #f8fafc;
  color: #334155;
  border-radius: 999px;
  padding: 0.42rem 0.8rem;
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-weight: 600;
}

.reaction-btn.active {
  background: #ede9fe;
  color: #5b21b6;
  border-color: rgba(139, 92, 246, 0.3);
}

.replies {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  margin-bottom: 0.8rem;
}

.reply-item {
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 12px;
  padding: 0.7rem 0.8rem;
  color: #334155;
}

.reply-author {
  display: inline-block;
  font-weight: 700;
  color: #0f172a;
  margin-right: 0.55rem;
}

.reply-form {
  display: flex;
  gap: 0.7rem;
}

.reply-form input {
  flex: 1;
  height: 42px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  padding: 0 0.8rem;
  background: #fff;
}

.reply-form button {
  border: 0;
  border-radius: 12px;
  background: #111827;
  color: #fff;
  padding: 0 1rem;
  font-weight: 700;
}

@media (max-width: 768px) {
  .hero-panel,
  .feature-grid {
    grid-template-columns: 1fr;
  }

  .wall-header,
  .composer-actions,
  .reply-form {
    flex-direction: column;
    align-items: stretch;
  }

  .card-accent {
    position: static;
    margin-top: 1rem;
  }
}
</style>

