/**
 * Based on https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2/tree/master/src/frontend/src/store/user.module.js
 */

export const user = {
  namespaced: true,
  state: {
    userData: null,
  },
  getters: {
    /**
     * Returns stored user
     * @param {object} state
     * @returns {object} user
     */
    getUser(state) {
      return state.userData;
    },
  },
  actions: {
    async setUser({ commit }, userData) {
      // Set user
      commit("setUser", userData);
    },
    forgetUser({ commit }) {
      commit("setUser", null);
    },
  },
  mutations: {
    setUser(state, userData) {
      state.userData = userData;
    },
  },
};
