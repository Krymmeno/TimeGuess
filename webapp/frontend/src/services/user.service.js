import axios from "axios";
import store from "@/store";

const API_USER_URL = `${process.env.VUE_APP_API_BASE}/users`;

/**
 * Gets all users
 * @returns {Promise} response
 */
export function getUsers() {
  return axios.get(API_USER_URL).then((response) => response.data);
}

/**
 * Gets all active users
 * @returns {Promise} response
 */
export function getActiveUsers() {
  return axios.get(`${API_USER_URL}/active`).then((response) => response.data);
}

/**
 * Post request to create a new user
 * @param user
 * @returns {Promise} response
 */
export function createUser(user) {
  return axios.post(API_USER_URL, user).then((response) => response.data);
}

/**
 * Updates a specific player
 * @param user
 * @returns {Promise} response
 */
export function updateUser(user) {
  return axios
    .put(`${API_USER_URL}/${user.userId}`, user)
    .then((response) => response.data);
}

/**
 * Gets all users and filters the list in order to search the given id
 * @param userId
 * @returns {Promise} response
 */
export async function getUser(userId) {
  let userLog = store.getters["user/getUser"];
  if (userLog.role !== "PLAYER") {
    return getUsers().then((users) => {
      let filtered = users.filter((user) => {
        return user.userId === userId;
      });
      if (filtered.length > 0) {
        return filtered[0];
      } else {
        return null;
      }
    });
  } else {
    return getActiveUsers().then((users) => {
      let filtered = users.filter((user) => {
        return user.userId === userId;
      });
      if (filtered.length > 0) {
        return filtered[0];
      } else {
        return null;
      }
    });
  }
}
