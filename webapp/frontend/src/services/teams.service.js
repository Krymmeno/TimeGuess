import axios from "axios";

const API_TEAMS_URL = `${process.env.VUE_APP_API_BASE}/gamerooms`;

/**
 * Puts a user in a specific team
 * @param userId
 * @param gameRoomId
 * @param teamColor
 * @returns {Promise} response
 */
export function putUserInTeam(userId, gameRoomId, teamColor) {
  return axios.put(
    `${API_TEAMS_URL}/${gameRoomId}/teams/users/${userId}`,
    null,
    {
      params: {
        teamColor,
      },
    }
  );
}

/**
 * Removes a user from their current team
 * @param userId
 * @param gameRoomId
 * @returns {Promise} response
 */
export function removeUserFromTeam(userId, gameRoomId) {
  return axios.delete(`${API_TEAMS_URL}/${gameRoomId}/teams/users/${userId}`);
}

/**
 * Creates a new team
 * @param gameRoomId
 * @returns {Promise} response
 */
export function createTeams(gameRoomId) {
  return axios.post(`${API_TEAMS_URL}/${gameRoomId}/teams`);
}

/**
 * Deletes a specific team
 * @param gameRoomId
 * @param teamColor
 * @returns {Promise} response
 */
export function deleteTeam(gameRoomId, teamColor) {
  return axios.delete(`${API_TEAMS_URL}/${gameRoomId}/teams`, {
    params: {
      teamColor,
    },
  });
}
