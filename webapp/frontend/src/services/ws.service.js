import SockJS from "sockjs-client";
import Stomp from "webstomp-client";

/**
 * Connects to a specific WebSocket with the given URL
 */
const WS = Stomp.over(new SockJS(`${process.env.VUE_APP_WS_BASE}`));

export default WS;
