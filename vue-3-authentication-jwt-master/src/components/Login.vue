<template>
  <div class="auth-shell">
    <div class="card card-container">
      <div class="card-header-block">
        <div class="profile-img-card">
          <font-awesome-icon icon="user" />
        </div>
        <h2>Welcome back</h2>
        <p>Sign in to continue</p>
      </div>

      <Form @submit="handleLogin" :validation-schema="schema" class="auth-form">
        <div class="form-group">
          <label for="username">Username</label>
          <Field name="username" type="text" class="form-control" placeholder="Enter your username" />
          <ErrorMessage name="username" class="error-feedback" />
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <Field name="password" type="password" class="form-control" placeholder="Enter your password" />
          <ErrorMessage name="password" class="error-feedback" />
        </div>

        <div class="form-group action-group">
          <button class="btn btn-primary btn-block" :disabled="loading">
            <span v-show="loading" class="spinner-border spinner-border-sm"></span>
            <span>Login</span>
          </button>
        </div>

        <div class="form-group">
          <div v-if="message" class="alert alert-danger" role="alert">
            {{ message }}
          </div>
        </div>
      </Form>
    </div>
  </div>
</template>

<script>
import { Form, Field, ErrorMessage } from "vee-validate";
import * as yup from "yup";

export default {
  name: "Login",
  components: {
    Form,
    Field,
    ErrorMessage,
  },
  data() {
    const schema = yup.object().shape({
      username: yup.string().required("Username is required!"),
      password: yup.string().required("Password is required!"),
    });

    return {
      loading: false,
      message: "",
      schema,
    };
  },
  computed: {
    loggedIn() {
      return this.$store.state.auth.status.loggedIn;
    },
  },
  created() {
    if (this.loggedIn) {
      this.$router.push("/profile");
    }
  },
  methods: {
    handleLogin(user) {
      this.loading = true;

      this.$store.dispatch("auth/login", user).then(
        () => {
          this.$router.push("/profile");
        },
        (error) => {
          this.loading = false;
          this.message =
            (error.response &&
              error.response.data &&
              error.response.data.message) ||
            error.message ||
            error.toString();
        }
      );
    },
  },
};
</script>

<style scoped>
.auth-shell {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 75vh;
}

.card-container {
  width: min(100%, 430px);
  padding: 2rem;
  border: 0;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.12);
}

.card-header-block {
  text-align: center;
  margin-bottom: 1.5rem;
}

.profile-img-card {
  width: 84px;
  height: 84px;
  margin: 0 auto 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 2rem;
  color: #fff;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  box-shadow: 0 18px 28px rgba(79, 70, 229, 0.25);
}

.card-header-block h2 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  letter-spacing: -0.05em;
  color: #111827;
}

.card-header-block p {
  margin: 0.5rem 0 0;
  color: #6b7280;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

label {
  display: block;
  margin-top: 0;
  margin-bottom: 0.45rem;
  font-weight: 600;
  color: #374151;
}

.form-control {
  border-radius: 14px;
  border: 1px solid #dfe7f3;
  padding: 0.8rem 0.9rem;
  background: #f9fbff;
  transition: all 0.2s ease;
}

.form-control:focus {
  border-color: #7c3aed;
  box-shadow: 0 0 0 0.2rem rgba(124, 58, 237, 0.15);
  background: #fff;
}

.action-group {
  margin-top: 0.6rem;
}

.btn-primary {
  border-radius: 14px;
  padding: 0.82rem 1.1rem;
  font-weight: 700;
  border: none;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  box-shadow: 0 18px 30px rgba(79, 70, 229, 0.18);
}

.error-feedback {
  display: block;
  margin-top: 0.4rem;
  color: #dc2626;
  font-size: 0.82rem;
}

.alert {
  border-radius: 14px;
  margin: 0;
}
</style>
