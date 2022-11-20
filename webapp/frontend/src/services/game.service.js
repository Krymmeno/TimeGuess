import axios from "axios";

const API_URL = `${process.env.VUE_APP_API_BASE}`;
const API_GAME_URL = `${process.env.VUE_APP_API_BASE}/games`;

/**
 * Post request to start a game
 * @param gameRoomId
 * @returns {Promise} response
 */
export function startGame(gameRoomId) {
  return axios
    .post(`${API_GAME_URL}/${gameRoomId}/startGame`)
    .then((response) => response.data);
}

/**
 * Post request to play the next round
 * @param gameId
 * @returns {Promise} response
 */
export function nextRound(gameId) {
  return axios
    .post(`${API_GAME_URL}/${gameId}/startNextRound`)
    .then((response) => response.data);
}

/**
 * Post request to set the result of the current round
 * @param gameId
 * @param result
 * @returns {Promise} response
 */
export function setGameRoundResult(gameId, result) {
  return axios
    .post(`${API_GAME_URL}/setGameRoundResult`, {
      gameId,
      result,
    })
    .then((response) => response.data);
}

/**
 * Gets the game with a specific path
 * @param path
 * @returns {Promise} response
 */
export function getGame(path) {
  return axios.get(`${API_URL}${path}`).then((response) => response.data);
}

/**
 * Post request to abort a running game
 * @param gameId
 * @returns {Promise} response
 */
export function abortGame(gameId) {
  return axios
    .post(`${API_GAME_URL}/${gameId}/abort`)
    .then((response) => response.data);
}
