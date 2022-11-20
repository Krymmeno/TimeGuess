/**
 * Based on https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2/tree/master/src/frontend/src/services/auth.service.js
 */

import axios from "axios";

const AUTH_URL = `${process.env.VUE_APP_API_BASE}/auth`;
const AUTHENTICATED_URL = `${process.env.VUE_APP_API_BASE}/users/me`;

/**
 * Fetches a new auth token
 * @param {string} username
 * @param {string} password
 * @returns {Promise} API Request
 */
export function login(username, password) {
  return axios
    .post(AUTH_URL, {
      username,
      password,
    })
    .then((response) => response.data);
}

/**
 * Validates an auth token
 * @returns {Promise} API Request
 */
export function getUser() {
  return axios.get(AUTHENTICATED_URL).then((response) => response.data);
}
