import axios from "axios";

const API_URL = `${process.env.VUE_APP_API_BASE}`;
const API_GAMEROOM_URL = `${process.env.VUE_APP_API_BASE}/gamerooms`;

/**
 * Gets all GameRooms
 * @returns {Promise} response
 */
export function getGameRooms() {
  return axios.get(API_GAMEROOM_URL).then((response) => response.data);
}

/**
 * Post request to create a new GameRoom
 * @param createGame
 * @returns {Promise} response
 */
export function createGameRoom(createGame) {
  return axios
    .post(API_GAMEROOM_URL, createGame)
    .then((response) => response.data);
}

/**
 * Marks a specific player as ready
 * @param gameRoomId
 * @param userId
 * @param isReady
 * @returns {Promise} response
 */
export function setReady(gameRoomId, userId, isReady) {
  return axios.patch(
    `${API_GAMEROOM_URL}/${gameRoomId}/users/${userId}/ready`,
    null,
    {
      params: {
        isReady,
      },
    }
  );
}

/**
 * Sets the topic to play
 * @param path
 * @param topicId
 * @returns {Promise} response
 */
export function setTopic(path, topicId) {
  return axios
    .patch(`${API_URL}${path}/topic`, null, {
      params: {
        topicId,
      },
    })
    .then((response) => response.data);
}

/**
 * Sets the points to win a game
 * @param path
 * @param maxPoints
 * @returns {Promise} response
 */
export function setMaxPoints(path, maxPoints) {
  return axios
    .patch(`${API_URL}${path}/maxPoints`, null, {
      params: {
        maxPoints,
      },
    })
    .then((response) => response.data);
}

/**
 * Gets a specific GameRoom
 * @param path
 * @returns {Promise} response
 */
export function getGameRoom(path) {
  return axios.get(`${API_URL}${path}`).then((response) => response.data);
}

/**
 * Leaves the current GameRoom
 * @param path
 * @param userId
 * @returns {Promise} response
 */
export function leaveGameRoom(path, userId) {
  return axios.delete(`${API_URL}${path}/users/${userId}`);
}

/**
 * Sets the TimeFlip to play with
 * @param path
 * @param timeFlipId
 * @returns {Promise} response
 */
export function setTimeFlip(path, timeFlipId) {
  return axios
    .patch(`${API_URL}${path}/timeflip/${timeFlipId}`, null)
    .then((response) => response.data);
}
