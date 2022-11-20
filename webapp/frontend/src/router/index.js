/**
 * Based on https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2/tree/master/src/frontend/src/router/index.js
 */

import Vue from "vue";
import VueRouter from "vue-router";
import store from "@/store";
import { getGameRoom } from "@/services/gameroom.service";
import { getGame } from "@/services/game.service";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "Root",
    redirect: "/gamelobby",
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("../views/Login.vue"),
  },
  {
    path: "/gamelobby",
    name: "GameLobby",
    component: () => import("../views/game/GameLobby.vue"),
    meta: {
      auth: true,
    },
  },
  {
    path: "/gamerooms",
    name: "GameRoom",
    redirect: "/gamelobby",
  },
  {
    path: "/gamerooms/:id",
    name: "GameRoom",
    component: () => import("../views/game/GameRoom.vue"),
    meta: {
      auth: true,
      needsInvite: true,
    },
  },
  {
    path: "/games",
    name: "Game",
    redirect: "/gamelobby",
  },
  {
    path: "/games/:id",
    name: "Game",
    component: () => import("../views/game/Game.vue"),
    meta: {
      auth: true,
      needsInvite: true,
    },
  },
  {
    path: "/dashboard",
    name: "Dashboard",
    component: () => import("../views/admin/Dashboard.vue"),
    meta: {
      auth: true,
    },
  },
  {
    path: "/profile",
    name: "Profile",
    component: () => import("../views/player/Profile.vue"),
    meta: {
      auth: true,
    },
  },
  {
    path: "/profile/:id",
    name: "Profile",
    component: () => import("../views/player/Profile.vue"),
    meta: {
      auth: true,
      role: ["ADMIN", "GAMEMANAGER", "PLAYER"],
    },
  },
  {
    path: "/userManagement",
    name: "User Management",
    component: () => import("../views/admin/UserManagement.vue"),
    meta: {
      auth: true,
      role: ["ADMIN", "GAMEMANAGER", "PLAYER"],
    },
  },
  {
    path: "/settings",
    name: "Settings",
    component: () => import("../views/player/UserSettings.vue"),
    meta: {
      auth: true,
    },
  },
  {
    path: "/settings/:id",
    name: "Settings",
    component: () => import("../views/player/UserSettings.vue"),
    meta: {
      auth: true,
      role: ["ADMIN", "GAMEMANAGER"],
    },
  },
  {
    path: "/game/settings",
    name: "Game Settings",
    component: () => import("../views/game/GameSettings.vue"),
    meta: {
      auth: true,
      role: ["ADMIN", "GAMEMANAGER"],
    },
  },
  {
    path: "/timeFlip/settings",
    name: "TimeFlip Settings",
    component: () => import("../views/timeflip/TimeFlipSettings.vue"),
    meta: {
      auth: true,
      role: ["ADMIN", "GAMEMANAGER"],
    },
  },
  {
    path: "/errors/404",
    name: "Error 404",
    component: () => import("../views/errors/404"),
    meta: {
      auth: false,
    },
  },
  {
    path: "/errors/500",
    name: "Error 500",
    component: () => import("../views/errors/500"),
    meta: {
      auth: false,
    },
  },
  {
    path: "/loading",
    name: "Loading",
    component: () => import("../views/Loading.vue"),
  },
  {
    path: "/team",
    name: "Team",
    component: () => import("../views/Team.vue"),
  },
  {
    path: "/tos",
    name: "Terms od Service",
    component: () => import("../views/TermsOfService.vue"),
  },
  {
    path: "/contact",
    name: "Contact",
    component: () => import("../views/Contact.vue"),
  },
  {
    path: "/*",
    name: "Error 404",
    redirect: "/errors/404",
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

router.beforeEach((to, from, next) => {
  if (to.meta.auth) {
    // check if user is already authenticated
    if (store.getters["auth/isLoggedIn"]) {
      const url = store.getters.getEntryUrl;

      // If entry url is set, redirect
      if (url) {
        store.commit("setEntryUrl", null);
        getGameRoom(url)
          .then(() => {
            router
              .push({
                path: url,
              })
              .catch(() => {});
          })
          .catch(() => {
            router.push({
              path: "/gamelobby",
            });
          });
        getGame(url)
          .then(() => {
            router
              .push({
                path: url,
              })
              .catch(() => {});
          })
          .catch(() => {
            router.push({
              path: "/gamelobby",
            });
          });
      } else {
        const { role } = store.getters["user/getUser"];
        if (!to.meta.role || to.meta.role.includes(role)) {
          next();
        } else {
          next("/errors/404");
        }
      }
    } else {
      // Store our entry url
      store.commit("setEntryUrl", to.path);

      // Try to log in by stored token or redirect to auth instead
      next(store.getters["auth/getToken"] ? "/loading" : "/login");
    }
  } else {
    next();
  }
});

router.afterEach((to) => {
  // Wait until dom is fully rendered
  Vue.nextTick(() => {
    // set document title
    document.title = to.meta.title ? to.meta.title : "TimeGuess";
  });
});

export default router;
