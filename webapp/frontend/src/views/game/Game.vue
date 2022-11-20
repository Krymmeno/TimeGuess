<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <v-row>
          <v-col class="d-flex">
            <h1 class="display-3 mt-16 mb-5">
              {{ game ? game.name : "" }}
            </h1>
            <v-spacer></v-spacer>
            <v-btn
              v-if="
                spectator &&
                this.$store.getters['user/getUser'].role === 'ADMIN'
              "
              color="error"
              dark
              class="align-self-end mb-5"
              @click="adminAbortGame"
            >
              Abort Game
            </v-btn>
          </v-col>
        </v-row>
        <v-divider class="mb-5"></v-divider>
        <v-dialog
          v-if="
            game
              ? !game.currentTeam.players.find(
                  (player) =>
                    player.userId === this.$store.getters['user/getUser'].userId
                ) && !spectator
              : false
          "
          v-model="guessedDialog"
          persistent
          max-width="500px"
        >
          <v-card>
            <v-card-title>
              <span class="headline">{{ $t("game.guessDialog.title") }}</span>
            </v-card-title>
            <v-divider class="mb-5"></v-divider>
            <v-card-text>
              <v-row>
                <v-col cols="12">
                  <v-btn block color="error" text @click="loseRound">
                    {{ $t("game.guessDialog.timeout") }}
                  </v-btn>
                </v-col>
                <v-col cols="12">
                  <v-btn block color="purple" text @click="reportViolation">
                    {{ $t("game.guessDialog.ruleViolation") }}
                  </v-btn>
                </v-col>
                <v-col cols="12">
                  <v-btn block color="success" text @click="winRound">
                    {{ $t("game.guessDialog.correctGuess") }}
                  </v-btn>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-dialog>
        <v-dialog
          v-if="winner"
          v-model="winDialog"
          persistent
          max-width="500px"
        >
          <v-card>
            <v-card-title>
              <span class="headline">
                {{ $t("game.winDialog.title") }} {{ winner.color }}
              </span>
            </v-card-title>
            <v-card-text
              v-if="
                winner.players.find(
                  (player) =>
                    player.userId === this.$store.getters['user/getUser'].userId
                )
              "
            >
              <lottie-animation
                path="json/trophy.json"
                :speed="1"
                :loop="false"
                :auto-play="true"
                :height="200"
                :width="200"
              />
            </v-card-text>
            <v-card-text>
              <v-alert
                v-if="
                  winner.players.find(
                    (player) =>
                      player.userId ===
                      this.$store.getters['user/getUser'].userId
                  )
                "
                color="success"
                dark
                dense
                icon="mdi-party-popper"
              >
                {{ $t("game.winDialog.winText") }}
              </v-alert>
              <v-alert
                v-else-if="!spectator"
                color="error"
                dark
                dense
                icon="mdi-emoticon-cry-outline"
              >
                {{ $t("game.winDialog.loseText") }}
              </v-alert>
              <v-alert
                v-else
                color="warning"
                dark
                dense
                icon="mdi-emoticon-excited-outline"
              >
                What a match!
              </v-alert>
            </v-card-text>
            <v-divider></v-divider>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="primary" text @click="leaveGame">
                {{ $t("game.winDialog.leaveGame") }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog v-model="abortDialog" persistent max-width="500px">
          <v-card>
            <v-card-title>
              <span class="headline">
                {{ $t("game.abortDialog.title") }}
              </span>
            </v-card-title>
            <v-card-text>{{ $t("game.abortDialog.abortText") }}</v-card-text>
            <v-divider></v-divider>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="primary" text @click="leaveGame">
                {{ $t("game.abortDialog.leaveGame") }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <IngameComponents
          :game="game ? game : null"
          :max-points="maxPoints"
          :round-time="roundTime"
          :round-timer="roundTimer"
          :max-time="maxTime"
          :spectator="spectator"
          @startRound="startRound"
          @winRound="winRound"
          @ruleViolation="reportViolation"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import IngameComponents from "@/components/game/gameroom/IngameComponents";
import WS from "@/services/ws.service";
import {
  abortGame,
  getGame,
  nextRound,
  setGameRoundResult,
} from "@/services/game.service";
import LottieAnimation from "lottie-vuejs/src/LottieAnimation.vue";

export default {
  name: "GameRoom",
  components: { IngameComponents, LottieAnimation },
  data() {
    return {
      game: null,
      guessedDialog: false,
      winDialog: false,
      winner: null,
      maxPoints: 0,
      roundTime: 0,
      roundTimer: 0,
      maxTime: 0,
      timer: null,
      gameSubscription: null,
      abortDialog: false,
      spectator: true,
    };
  },
  mounted() {
    this.updateGame();
    if (WS.connected) {
      this.subscribeToGame();
    } else {
      WS.connect(
        {
          Authorization: `Bearer ${this.$store.getters["auth/getToken"]}`,
        },
        () => {
          this.subscribeToGame();
        },
        (error) => {
          console.log(error);
        }
      );
    }
    window.addEventListener("beforeunload", this.beforeUnloadListener);
  },
  methods: {
    updateGame() {
      getGame(this.$route.path).then((response) => {
        this.game = response;
        switch (response.maxPoints) {
          case "TEN":
            this.maxPoints = 10;
            break;
          case "TWENTY":
            this.maxPoints = 20;
            break;
          case "THIRTY":
            this.maxPoints = 30;
            break;
          case "FORTY":
            this.maxPoints = 40;
            break;
          case "FIFTY":
            this.maxPoints = 50;
            break;
          case "SIXTY":
            this.maxPoints = 60;
            break;
          case "SEVENTY":
            this.maxPoints = 70;
            break;
          default:
            this.maxPoints = 0;
            break;
        }
        switch (response.currentRound.time) {
          case "ONE":
            this.roundTime = 1;
            this.roundTimer = this.maxTime = 60;
            break;
          case "TWO":
            this.roundTime = 2;
            this.roundTimer = this.maxTime = 120;
            break;
          case "THREE":
            this.roundTime = 3;
            this.roundTimer = this.maxTime = 180;
            break;
          default:
            this.roundTime = 0;
            this.roundTimer = this.maxTime = 0;
            break;
        }
        if (
          response.currentRound.guessStart === null &&
          response.currentRound.guessEnd === null
        ) {
          // before round
          this.stopTimer();
          this.guessedDialog = false;
        } else if (
          response.currentRound.guessStart !== null &&
          response.currentRound.guessEnd === null
        ) {
          // round running
          this.roundTimer =
            this.roundTimer -
            Math.floor(response.currentRound.guessTimeMillis / 1000);
          this.startTimer();
          this.guessedDialog = false;
        } else if (
          response.currentRound.guessStart !== null &&
          response.currentRound.guessEnd !== null
        ) {
          // round end
          this.roundTimer =
            this.roundTimer -
            Math.floor(response.currentRound.guessTimeMillis / 1000);
          this.stopTimer();
          this.guessedDialog = response.currentRound.result === null;
        } else {
          this.stopTimer();
          this.guessedDialog = false;
        }
        if (response.winner !== null) {
          this.winner = response.winner;
          this.winDialog = true;
        }
        this.game.teams.forEach((team) => {
          if (
            team.players.some(
              (player) =>
                player.userId === this.$store.getters["user/getUser"].userId
            )
          ) {
            this.spectator = false;
          }
        });
      });
    },
    adminAbortGame() {
      abortGame(this.game.gameId);
    },
    abortGame() {
      this.stopTimer();
      this.abortDialog = true;
    },
    leaveGame() {
      this.$router.push("/gamelobby");
    },
    subscribeToGame() {
      this.gameSubscription = WS.subscribe(
        `/topic${this.$route.path}`,
        (tick) => {
          if (tick.body === "GAME_UPDATE") {
            this.updateGame();
          } else if (tick.body === "GAME_ABORT" && this.game.winner === null) {
            this.abortGame();
          }
        }
      );
    },
    unsubscribeFromGame() {
      this.gameSubscription.unsubscribe();
      this.gameSubscription = null;
    },
    startRound() {
      nextRound(this.game.gameId);
    },
    winRound() {
      setGameRoundResult(this.game.gameId, "WIN");
      this.guessedDialog = false;
    },
    reportViolation() {
      setGameRoundResult(this.game.gameId, "RULE_VIOLATION");
      this.guessedDialog = false;
    },
    loseRound() {
      setGameRoundResult(this.game.gameId, "TIMEOUT");
      this.guessedDialog = false;
    },
    startTimer() {
      this.timer = setInterval(() => {
        this.roundTimer--;
        if (this.roundTimer === 0) {
          clearInterval(this.timer);
        }
      }, 1000);
    },
    stopTimer() {
      clearInterval(this.timer);
    },
    beforeUnloadListener(e) {
      e.returnValue = "Are you sure you want to leave?";
    },
  },
  beforeRouteLeave(to, from, next) {
    this.unsubscribeFromGame();
    next();
  },
  destroyed() {
    window.removeEventListener("beforeunload", this.beforeUnloadListener);
  },
};
</script>
