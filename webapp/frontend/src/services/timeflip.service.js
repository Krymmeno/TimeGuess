import axios from "axios";

const TIMEFLIP_URL = `${process.env.VUE_APP_API_BASE}/timeflip`;

/**
 * Gets all TimeFlips
 * @returns {Promise} response
 */
export function getTimeFlips() {
  return axios.get(TIMEFLIP_URL).then((response) => response.data);
}

/**
 * Gets all available TimeFlips
 * @returns {Promise} response
 */
export function getAvailableTimeFlips() {
  return axios
    .get(`${TIMEFLIP_URL}/available`)
    .then((response) => response.data);
}

/**
 * Updates a specific TimeFlip
 * @param timeFlip
 * @returns {Promise} response
 */
export function updateTimeFlip(timeFlip) {
  return axios
    .post(
      `${TIMEFLIP_URL}/${timeFlip.timeFlipId}`,
      Object.fromEntries(timeFlip.timeFlipFacetMap)
    )
    .then((response) => response.data);
}
