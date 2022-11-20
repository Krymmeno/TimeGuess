import axios from "axios";

const API_INVITE_URL = `${process.env.VUE_APP_API_BASE}/invites`;

/**
 * Gets all invites of a specific user
 * @param userId
 * @returns {Promise} response
 */
export function getInvites(userId) {
  return axios
    .get(`${API_INVITE_URL}/${userId}`)
    .then((response) => response.data);
}

/**
 * Creates an invite for a specific player
 * @param gameRoomId
 * @param userIdList
 * @returns {Promise} response
 */
export function createInvite(gameRoomId, userIdList) {
  return axios
    .post(API_INVITE_URL, {
      gameRoomId,
      userIdList,
    })
    .then((response) => response.data);
}

/**
 * Accepts an invite to a specific game
 * @param inviteId
 * @returns {Promise} response
 */
export function acceptInvite(inviteId) {
  return axios
    .post(`${API_INVITE_URL}/${inviteId}/accept`)
    .then((response) => response.data);
}
