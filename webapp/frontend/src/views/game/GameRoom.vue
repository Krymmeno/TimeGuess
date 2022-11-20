<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <h1 class="display-3 mt-16 mb-5">
          {{ gameRoom ? gameRoom.name : "" }}
        </h1>
        <v-divider class="mb-5"></v-divider>
        <v-alert
          v-if="
            gameRoom
              ? gameRoomErrors !== '' &&
                gameRoom.gameHostId ===
                  this.$store.getters['user/getUser'].userId
              : false
          "
          dense
          text
          type="info"
          class="mt-3"
        >
          <ul>
            <li :key="index" v-for="(gameError, index) in gameRoomErrors">
              {{ $t(`gameRoom.gameErrors.${gameError}`) }}
            </li>
          </ul>
        </v-alert>
        <PregameComponents
          :gameRoom="gameRoom ? gameRoom : null"
          :topic="gameRoom ? gameRoom.topic.topicId : null"
          :availableTopics="availableTopics"
          :timeFlip="
            gameRoom
              ? gameRoom.timeFlip
                ? gameRoom.timeFlip.timeFlipId
                : null
              : null
          "
          :max-points="gameRoom ? gameRoom.maxPoints : null"
          :spectator="spectator"
          @leaveRoom="leaveGameRoom"
          @startGame="startGame"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import PregameComponents from "@/components/game/gameroom/PregameComponents";
import { getGameRoom, leaveGameRoom } from "@/services/gameroom.service";
import { getAvailableTopics } from "@/services/topics.service";
import WS from "@/services/ws.service";
import { startGame } from "@/services/game.service";

//let timerInterval;
export default {
  name: "GameRoom",
  components: { PregameComponents },
  data() {
    return {
      gameRoom: null,
      availableTopics: [],
      timeFlips: [],
      spectator: false,
      gameRoomSubscription: null,
      gameRoomErrors: [],
    };
  },
  mounted() {
    this.updateGameRoom();
    if (WS.connected) {
      this.subscribeToGameRoom();
    } else {
      WS.connect(
        {
          Authorization: `Bearer ${this.$store.getters["auth/getToken"]}`,
        },
        () => {
          this.subscribeToGameRoom();
        },
        (error) => {
          console.log(error);
        }
      );
    }
    window.addEventListener("beforeunload", this.beforeUnloadListener);
  },
  methods: {
    startGame() {
      startGame(this.gameRoom.gameRoomId);
    },
    async loadTopics() {
      await getAvailableTopics().then((topics) => {
        this.availableTopics = [];
        topics.forEach((topic) => {
          this.availableTopics.push({
            text: topic.name,
            value: topic.topicId,
          });
        });
      });
    },
    updateGameRoom() {
      getGameRoom(this.$route.path)
        .then((response) => {
          this.gameRoomErrors = "";
          this.gameRoom = response;
          if (this.gameRoom.gameCreationError !== "") {
            this.gameRoomErrors = this.gameRoom.gameCreationError.split(",");
          }
          this.loadTopics();
          if (response.gameStarted) {
            this.$router
              .push(`/games/${this.gameRoom.gameRoomId}`)
              .catch(() => {});
          }
          let isPartOfGame =
            this.gameRoom.gameRoomUsers.some(
              (user) =>
                user.user.userId === this.$store.getters["user/getUser"].userId
            ) ||
            this.gameRoom.invitedUsers.some(
              (user) =>
                user.userId === this.$store.getters["user/getUser"].userId
            );
          this.spectator = !isPartOfGame;
        })
        .catch(() => this.leaveGameRoom());
    },
    subscribeToGameRoom() {
      this.gameRoomSubscription = WS.subscribe(
        `/topic${this.$route.path}`,
        (tick) => {
          if (tick.body === "GAME_ROOM_UPDATE") {
            this.updateGameRoom();
          }
        }
      );
    },
    unsubscribeFromGameRoom() {
      this.gameRoomSubscription.unsubscribe();
      this.gameRoomSubscription = null;
    },
    leaveGameRoom() {
      leaveGameRoom(
        this.$route.path,
        this.$store.getters["user/getUser"].userId
      );
      this.$router.push("/gamelobby");
    },
    beforeUnloadListener(e) {
      e.returnValue = "Are you sure you want to leave?";
    },
  },
  beforeRouteLeave(to, from, next) {
    this.unsubscribeFromGameRoom();
    if (to.path !== `/games/${this.gameRoom.gameRoomId}`) {
      leaveGameRoom(
        this.$route.path,
        this.$store.getters["user/getUser"].userId
      );
    }
    next();
  },
  destroyed() {
    window.removeEventListener("beforeunload", this.beforeUnloadListener);
  },
};
</script>
