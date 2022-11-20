import axios from "axios";

const TERM_URL = `${process.env.VUE_APP_API_BASE}/terms`;

/**
 * Post request to add a new term
 * @param topicId
 * @param name
 * @returns {Promise} response
 */
export function addTerm(topicId, name) {
  return axios
    .post(TERM_URL, { topicId, name })
    .then((response) => response.data);
}

/**
 * Updates the name of a specific term
 * @param termId
 * @param termName
 * @returns {Promise} response
 */
export function updateTerm(termId, termName) {
  return axios
    .put(`${TERM_URL}/${termId}`, null, { params: { termName } })
    .then((response) => response.data);
}

/**
 * Deletes a specific term
 * @param termId
 * @returns {Promise} response
 */
export function deleteTerm(termId) {
  return axios
    .delete(`${TERM_URL}/${termId}`)
    .then((response) => response.data);
}
