<template>
  <v-card>
    <v-card-title>
      {{ team }}
      <v-spacer></v-spacer>
      <v-btn
        icon
        v-if="gameHostId === this.$store.getters['user/getUser'].userId"
      >
        <v-icon color="error" @click="deleteTeam(team)">
          mdi-delete-outline
        </v-icon>
      </v-btn>
    </v-card-title>
    <v-divider></v-divider>
    <v-card-text>
      <v-list
        v-if="
          allPlayers.filter((player) => player.teamColor === this.team)
            .length !== 0
        "
      >
        <v-list-item
          :key="index"
          v-for="(player, index) in allPlayers.filter(
            (player) => player.teamColor === this.team
          )"
        >
          <v-list-item-icon v-if="player.user.userId === gameHostId">
            <v-icon> mdi-star </v-icon>
          </v-list-item-icon>
          <v-list-item-icon v-else-if="player.ready">
            <v-icon> mdi-check </v-icon>
          </v-list-item-icon>
          <v-list-item-icon v-else>
            <v-icon> mdi-account-clock </v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            {{ player.user.username }}
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              v-if="
                gameHostId === $store.getters['user/getUser'].userId &&
                player.user.userId !== gameHostId
              "
              icon
              @click="kickPlayer(player.user.userId)"
            >
              <v-icon color="error">mdi-close</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <v-alert
        v-if="
          allPlayers.filter((player) => player.teamColor === this.team)
            .length === 0
        "
        dense
        text
        type="info"
        class="mt-3"
      >
        {{ $t("gameRoom.pregame.teamList.noPlayers") }}
      </v-alert>
      <div v-if="!spectator">
        <v-btn
          v-if="
            allPlayers
              .filter((player) => player.teamColor === this.team)
              .find(
                (player) =>
                  player.user.userId ===
                  this.$store.getters['user/getUser'].userId
              )
          "
          color="error"
          block
          @click="leaveTeam"
        >
          {{ $t("gameRoom.pregame.teamList.leaveTeam") }}
        </v-btn>
        <v-btn v-else color="primary" block @click="joinTeam">
          {{ $t("gameRoom.pregame.teamList.joinTeam") }}
        </v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>

<script>
import {
  deleteTeam,
  putUserInTeam,
  removeUserFromTeam,
} from "@/services/teams.service";
import { leaveGameRoom } from "@/services/gameroom.service";

export default {
  name: "Team",
  props: {
    gameRoomId: Number,
    gameHostId: Number,
    allPlayers: Array,
    team: String,
    spectator: Boolean,
  },
  methods: {
    deleteTeam(team) {
      deleteTeam(this.gameRoomId, team);
    },
    joinTeam() {
      putUserInTeam(
        this.$store.getters["user/getUser"].userId,
        this.gameRoomId,
        this.team
      );
    },
    leaveTeam() {
      removeUserFromTeam(
        this.$store.getters["user/getUser"].userId,
        this.gameRoomId
      );
    },
    kickPlayer(userId) {
      leaveGameRoom(`/gamerooms/${this.gameRoomId}`, userId);
      this.$notify({
        title: this.$t("notification.success"),
        text: this.$t("notification.kickSuccess"),
        type: "success",
      });
    },
  },
};
</script>
