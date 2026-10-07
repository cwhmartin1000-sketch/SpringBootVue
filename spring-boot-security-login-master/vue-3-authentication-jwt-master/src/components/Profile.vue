<template>
  <div class="profile-page">
    <section class="profile-card">
      <div class="profile-header">
        <div class="avatar-ring">
          <font-awesome-icon icon="user" />
        </div>
        <div>
          <span class="eyebrow">Profile</span>
          <h1>{{ currentUser.username }}</h1>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-block">
          <span class="label">User ID</span>
          <strong>{{ currentUser.id }}</strong>
        </div>
        <div class="info-block">
          <span class="label">Email</span>
          <strong>{{ currentUser.email }}</strong>
        </div>
      </div>

      <div class="token-box">
        <div class="token-label">Access token</div>
        <code>{{ currentUser?.accessToken?.substring(0, 20) }} ... {{ currentUser?.accessToken?.substring(currentUser.accessToken.length - 20) }}</code>
      </div>

      <div class="authorities">
        <div class="label">Authorities</div>
        <div class="role-list">
          <span v-for="role in currentUser.roles" :key="role" class="role-chip">{{ role }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'Profile',
  computed: {
    currentUser() {
      return this.$store.state.auth.user;
    }
  },
  mounted() {
    if (!this.currentUser) {
      this.$router.push('/login');
    }
  }
};
</script>

<style scoped>
.profile-page {
  display: flex;
  justify-content: center;
  padding-top: 1rem;
}

.profile-card {
  width: min(100%, 820px);
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 28px;
  padding: 2rem;
  box-shadow: 0 22px 40px rgba(15, 23, 42, 0.08);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.avatar-ring {
  width: 68px;
  height: 68px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f46e5, #8b5cf6);
  color: #fff;
  font-size: 1.7rem;
  box-shadow: 0 18px 28px rgba(79, 70, 229, 0.18);
}

.eyebrow {
  display: block;
  font-size: 0.75rem;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #7c3aed;
  font-weight: 700;
}

.profile-header h1 {
  margin: 0.2rem 0 0;
  font-size: clamp(2rem, 3vw, 2.6rem);
  letter-spacing: -0.05em;
  color: #111827;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.info-block,
.token-box,
.authorities {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  padding: 1rem 1.1rem;
}

.label,
.token-label {
  display: block;
  font-size: 0.78rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #64748b;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.info-block strong {
  color: #0f172a;
  font-size: 1.05rem;
}

.token-box {
  margin-bottom: 1.5rem;
}

.token-box code {
  display: block;
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 12px;
  padding: 0.8rem 1rem;
  overflow-wrap: anywhere;
  font-size: 0.82rem;
}

.role-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.role-chip {
  padding: 0.55rem 0.8rem;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.12), rgba(14, 165, 233, 0.12));
  color: #312e81;
  font-weight: 600;
}

@media (max-width: 640px) {
  .profile-card {
    padding: 1.2rem;
  }

  .profile-header {
    align-items: flex-start;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
