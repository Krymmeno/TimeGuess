<template>
  <v-container>
    <v-row>
      <v-col cols="12" sm="4">
        <v-btn
          v-if="gameHostId === this.$store.getters['user/getUser'].userId"
          block
          color="success"
          @click="$emit('startGame')"
          :disabled="
            !allPlayersReady || timeFlip === null || gameCreationError !== ''
          "
        >
          <v-icon left> mdi-rocket-outline </v-icon>
          {{ $t("gameRoom.pregame.startGame") }}
        </v-btn>
        <v-btn
          v-else
          block
          color="success"
          @click="setReady"
          :disabled="
            players
              .filter((player) => player.teamColor === null)
              .some(
                (player) =>
                  player.user.userId ===
                  this.$store.getters['user/getUser'].userId
              ) ||
            players
              .filter((player) => player.teamColor !== null)
              .some(
                (player) =>
                  player.user.userId ===
                    this.$store.getters['user/getUser'].userId && player.ready
              )
          "
        >
          <v-icon left> mdi-rocket-outline </v-icon>
          {{ $t("gameRoom.pregame.ready") }}
        </v-btn>
      </v-col>
      <v-col cols="12" sm="4">
        <v-btn
          v-if="gameHostId === this.$store.getters['user/getUser'].userId"
          color="warning"
          block
          @click="createTeam"
        >
          <v-icon left> mdi-creation </v-icon>
          {{ $t("gameRoom.pregame.createTeam.title") }}
        </v-btn>
      </v-col>
      <v-col cols="12" sm="4">
        <v-btn color="error" block @click="$emit('leaveRoom')">
          <v-icon left> mdi-logout </v-icon>
          {{ $t("gameRoom.pregame.leaveRoom") }}
        </v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { createTeams } from "@/services/teams.service";
import { setReady } from "@/services/gameroom.service";

export default {
  name: "GameRoomActions",
  props: {
    gameRoomId: Number,
    gameHostId: Number,
    timeFlip: Number,
    players: Array,
    gameCreationError: String,
    allPlayersReady: Boolean,
  },
  methods: {
    setReady() {
      setReady(
        this.gameRoomId,
        this.$store.getters["user/getUser"].userId,
        true
      );
    },
    createTeam() {
      createTeams(this.gameRoomId);
    },
  },
};
</script>
