/**
 * Based on https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2/tree/master/src/frontend/src/store/auth.module.js
 */

import axios from "axios";
import { login, getUser } from "@/services/auth.service";

export const auth = {
  namespaced: true,
  state: {
    token: localStorage.getItem("accessToken"),
    loggedIn: false,
  },
  getters: {
    /**
     * Returns whether user is logged in or not
     * @param {object} state
     * @returns {boolean} isLoggedIn
     */
    isLoggedIn(state) {
      return state.loggedIn;
    },
    /**
     * Returns token
     * @param {object} state
     * @returns {string} token
     */
    getToken(state) {
      return state.token;
    },
  },
  actions: {
    async login({ commit, dispatch }, user) {
      return login(user.name, user.password).then((response) => {
        // Set token
        localStorage.setItem("accessToken", response.accessToken);

        commit("setToken", response.accessToken);

        return dispatch("auth/authenticate", null, { root: true });
      });
    },
    authenticate({ commit, dispatch, getters }) {
      axios.defaults.headers.common.Authorization = `Bearer ${getters.getToken}`;
      return getUser().then((response) =>
        dispatch("user/setUser", response, { root: true }).then(() => {
          // Set logged in after everything is loaded
          commit("setLoggedIn");
        })
      );
    },
    logout({ commit, dispatch }) {
      // Remove token
      delete axios.defaults.headers.common.Authorization;
      localStorage.removeItem("accessToken");

      dispatch("user/forgetUser", null, { root: true });

      commit("logout");
    },
  },
  mutations: {
    setLoggedIn(state) {
      state.loggedIn = true;
    },
    setToken(state, token) {
      state.token = token;
    },
    logout(state) {
      state.loggedIn = false;
      state.token = null;
    },
  },
};
