<template>
  <div id="app">
    <nav class="navbar navbar-expand navbar-dark topbar">
      <div class="container app-shell">
        <router-link to="/home" class="navbar-brand brand-mark">
          <span class="brand-icon"><font-awesome-icon icon="shield-alt" /></span>
          <span>午休聊天室</span>
        </router-link>

        <div class="navbar-nav mr-auto nav-links">
          <li class="nav-item">
            <router-link to="/home" class="nav-link">
              <font-awesome-icon icon="home" /> 首頁
            </router-link>
          </li>
          <li v-if="currentUser" class="nav-item">
            <router-link to="/user" class="nav-link">
              <font-awesome-icon icon="comment" /> 留言版
            </router-link>
          </li>
        </div>

        <div v-if="!currentUser" class="navbar-nav ml-auto auth-links">
          <li class="nav-item">
            <router-link to="/login" class="nav-link btn btn-outline-light btn-sm action-btn">
              <font-awesome-icon icon="sign-in-alt" /> 登入
            </router-link>
          </li>
        </div>

        <div v-if="currentUser" class="navbar-nav ml-auto auth-links user-menu">
          <li class="nav-item">
            <router-link to="/user" class="nav-link profile-pill">
              <span class="user-avatar"><font-awesome-icon icon="user" /></span>
              {{ currentUser.username }}
            </router-link>
          </li>
          <li class="nav-item">
            <a class="nav-link logout-link" @click.prevent="logOut">
              <font-awesome-icon icon="sign-out-alt" /> 登出
            </a>
          </li>
        </div>
      </div>
    </nav>

    <main class="page-shell">
      <div class="container app-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script>
export default {
  computed: {
    currentUser() {
      return this.$store.state.auth.user;
    }
  },
  methods: {
    logOut() {
      this.$store.dispatch('auth/logout');
      this.$router.push('/login');
    }
  }
};
</script>

<style scoped>
#app {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7ff 0%, #edf3ff 35%, #f8f9fc 100%);
}

.topbar {
  background: rgba(15, 23, 42, 0.92);
  backdrop-filter: blur(12px);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.12);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  position: sticky;
  top: 0;
  z-index: 10;
}

.app-shell {
  display: flex;
  align-items: center;
  min-height: 80px;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  letter-spacing: 0.03em;
  color: #fff !important;
  font-size: 1.15rem;
}

.brand-icon {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  box-shadow: 0 12px 25px rgba(99, 102, 241, 0.35);
}

.nav-links,
.auth-links {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.nav-link {
  color: rgba(255, 255, 255, 0.82) !important;
  font-size: 0.96rem;
  border-radius: 10px;
  padding: 0.7rem 0.9rem !important;
  transition: all 0.2s ease;
}

.nav-link:hover,
.nav-link.router-link-active {
  background: rgba(255, 255, 255, 0.08);
  color: #fff !important;
}

.action-btn {
  border-radius: 999px;
  padding: 0.6rem 1.1rem !important;
  font-weight: 600;
}

.profile-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.07);
  border-radius: 999px;
  padding: 0.5rem 0.8rem !important;
}

.user-avatar {
  width: 28px;
  height: 28px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.14);
  border-radius: 50%;
}

.logout-link {
  cursor: pointer;
  color: #ffd7d7 !important;
}

.page-shell {
  padding: 2rem 0 3rem;
}

.app-content {
  padding-top: 0.5rem;
}
</style>
