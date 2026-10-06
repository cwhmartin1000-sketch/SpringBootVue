<template>
  <div class="board-page">
    <section class="board-card">
      <span class="eyebrow">Moderator access</span>
      <h1>Moderation overview</h1>
      <p>{{ content }}</p>
    </section>
  </div>
</template>

<script>
import UserService from "../services/user.service";

export default {
  name: "Moderator",
  data() {
    return {
      content: "",
    };
  },
  mounted() {
    UserService.getModeratorBoard().then(
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
.board-page {
  display: flex;
  justify-content: center;
  padding-top: 1rem;
}

.board-card {
  width: min(100%, 720px);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 28px;
  padding: 2rem;
  box-shadow: 0 22px 40px rgba(15, 23, 42, 0.08);
}

.eyebrow {
  display: block;
  margin-bottom: 0.5rem;
  color: #14b8a6;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  font-weight: 700;
  font-size: 0.72rem;
}

h1 {
  margin: 0 0 0.8rem;
  font-size: clamp(2rem, 3vw, 2.8rem);
  letter-spacing: -0.05em;
  color: #111827;
}

p {
  margin: 0;
  color: #4b5563;
  line-height: 1.8;
  font-size: 1.02rem;
}
</style>
