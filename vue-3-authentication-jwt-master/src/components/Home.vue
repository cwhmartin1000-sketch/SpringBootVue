<template>
  <div class="home-page">
    <section class="hero-panel">
      <div class="hero-copy">
        <span class="eyebrow">Modern application</span>
        <h1>Build secure experiences people enjoy using.</h1>
        <p>
          {{ content || 'Welcome to the Spring Boot + Vue authentication dashboard.' }}
        </p>
        <div class="hero-actions">
          <router-link to="/register" class="btn btn-primary primary-btn">Create account</router-link>
          <router-link to="/login" class="btn btn-outline-primary secondary-btn">Sign in</router-link>
        </div>
      </div>

      <div class="hero-visual">
        <div class="floating-card card-main">
          <div class="mini-header">
            <span class="status-dot" />
            <span>System health</span>
          </div>
          <div class="metric-row">
            <div>
              <small>Uptime</small>
              <strong>99.9%</strong>
            </div>
            <div class="pill success">Healthy</div>
          </div>
          <div class="chart-bars">
            <span style="height: 30%"></span>
            <span style="height: 55%"></span>
            <span style="height: 70%"></span>
            <span style="height: 52%"></span>
            <span style="height: 82%"></span>
            <span style="height: 100%"></span>
          </div>
        </div>

        <div class="floating-card card-accent">
          <small>Active users</small>
          <strong>2,480</strong>
          <span class="trend">+12.5% this week</span>
        </div>
      </div>
    </section>

    <section class="feature-grid">
      <article class="feature-card">
        <div class="icon-wrap purple"><font-awesome-icon icon="lock" /></div>
        <h3>Secure access</h3>
        <p>JWT-based authentication keeps sessions protected and easy to manage.</p>
      </article>
      <article class="feature-card">
        <div class="icon-wrap blue"><font-awesome-icon icon="bolt" /></div>
        <h3>Fast workflows</h3>
        <p>Streamlined pages and clear state management reduce friction for app users.</p>
      </article>
      <article class="feature-card">
        <div class="icon-wrap green"><font-awesome-icon icon="chart-line" /></div>
        <h3>Clear insights</h3>
        <p>Readable dashboards give teams fast visibility into app health and adoption.</p>
      </article>
    </section>
  </div>
</template>

<script>
import UserService from "../services/user.service";

export default {
  name: "Home",
  data() {
    return {
      content: "",
    };
  },
  mounted() {
    UserService.getPublicContent().then(
      (response) => {
        this.content = response.data;
      },
      (error) => {
        this.content =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
      }
    );
  },
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

.hero-copy, .hero-visual {
  position: relative;
  z-index: 1;
}

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
  box-shadow: 0 0 0 6px rgba(52, 211, 153, 0.15);
}

.metric-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
}

.metric-row small {
  display: block;
  color: rgba(191, 219, 254, 0.8);
}

.metric-row strong {
  font-size: 2rem;
  letter-spacing: -0.06em;
}

.pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.35rem 0.7rem;
  border-radius: 999px;
  font-size: 0.77rem;
  font-weight: 700;
}

.pill.success {
  background: rgba(52, 211, 153, 0.15);
  color: #a7f3d0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 0.55rem;
  height: 98px;
}

.chart-bars span {
  flex: 1;
  border-radius: 999px 999px 0 0;
  background: linear-gradient(180deg, #7dd3fc, #3b82f6);
}

.card-accent {
  position: absolute;
  right: 0;
  bottom: 24px;
  display: flex;
  flex-direction: column;
  padding: 1rem 1.1rem;
  color: #e2e8f0;
  transform: translateX(10px);
}

.card-accent small {
  color: rgba(148, 163, 184, 0.9);
}

.card-accent strong {
  font-size: 1.8rem;
  letter-spacing: -0.06em;
}

.trend {
  color: #86efac;
  font-size: 0.75rem;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1.2rem;
}

.feature-card {
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 22px;
  padding: 1.5rem;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
}

.icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  font-size: 1.3rem;
  color: white;
}

.icon-wrap.purple { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }
.icon-wrap.blue { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.icon-wrap.green { background: linear-gradient(135deg, #10b981, #6ee7b7); }

.feature-card h3 {
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 0.65rem;
  color: #1f2937;
}

.feature-card p {
  color: #6b7280;
  margin: 0;
  line-height: 1.7;
}

@media (max-width: 992px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
