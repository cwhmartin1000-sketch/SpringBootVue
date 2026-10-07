import { createRouter, createWebHistory } from "vue-router";
import Home from "./components/Home.vue";
import Login from "./components/Login.vue";
import Register from "./components/Register.vue";
const BoardUser = () => import("./components/BoardUser.vue")

const routes = [
  {
    path: "/",
    name: "home",
    component: Home,
  },
  {
    path: "/home",
    component: Home,
  },
  {
    path: "/login",
    component: Login,
  },
  {
    path: "/register",
    component: Register,
  },
  {
    path: "/user",
    name: "user",
    component: BoardUser,
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const storedUser = localStorage.getItem("user");
  let loggedIn = false;

  if (storedUser) {
    try {
      loggedIn = Boolean(JSON.parse(storedUser).accessToken);
    } catch (error) {
      loggedIn = false;
    }
  }

  if (to.matched.some((record) => record.meta.requiresAuth) && !loggedIn) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }

  return true;
});

export default router;