import axios from "axios";

const STAT_URL = `${process.env.VUE_APP_API_BASE}/statistics`;

/**
 * Gets all game statistics
 * @returns {Promise} response
 */
export function getGameStatistics() {
  return axios.get(`${STAT_URL}/games`).then((response) => response.data);
}

/**
 * Gets all user statistics
 * @param userId
 * @returns {Promise} response
 */
export function getUserStatistics(userId) {
  return axios
    .get(`${STAT_URL}/users/${userId}`)
    .then((response) => response.data);
}
