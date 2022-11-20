/**
 * Based on https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2/tree/master/src/frontend/src/store/index.js
 */

import Vue from "vue";
import Vuex from "vuex";
import { auth } from "./auth.module";
import { user } from "./user.module";

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    entryUrl: null,
  },
  getters: {
    getEntryUrl(state) {
      return state.entryUrl;
    },
  },
  actions: {},
  mutations: {
    setEntryUrl(state, url) {
      state.entryUrl = url;
    },
  },
  modules: {
    auth,
    user,
  },
});
