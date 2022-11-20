import axios from "axios";

const API_TOPIC_URL = `${process.env.VUE_APP_API_BASE}/topics`;

/**
 * Updates a specific topic
 * @param topicId
 * @param topicName
 * @returns {Promise} response
 */
export function updateTopic(topicId, topicName) {
  return axios
    .put(`${API_TOPIC_URL}/${topicId}`, null, { params: { topicName } })
    .then((response) => response.data);
}

/**
 * Deletes a specific topic
 * @param topicId
 * @returns {Promise} response
 */
export function deleteTopic(topicId) {
  return axios
    .delete(`${API_TOPIC_URL}/${topicId}`)
    .then((response) => response.data);
}

/**
 * Gets all topics
 * @returns {Promise} response
 */
export function getTopics() {
  return axios.get(API_TOPIC_URL).then((response) => response.data);
}

/**
 * Post request to add a new topic
 * @param topicName
 * @returns {Promise} response
 */
export function addTopic(topicName) {
  return axios
    .post(API_TOPIC_URL, null, { params: { topicName } })
    .then((response) => response.data);
}

/**
 * Imports terms for a specific topic
 * @param topicId
 * @param terms
 * @returns {Promise} response
 */
export function importTerms(topicId, terms) {
  return axios
    .post(`${API_TOPIC_URL}/${topicId}/terms`, terms)
    .then((response) => response.data);
}

/**
 * Gets all available topics
 * @returns {Promise} response
 */
export function getAvailableTopics() {
  return axios
    .get(`${API_TOPIC_URL}/available`)
    .then((response) => response.data);
}
