<template>
  <v-sheet rounded elevation="2">
    <h3 class="heading text-center py-2">
      {{ $t("gameRoom.pregame.invitedList.title") }}
    </h3>
    <v-divider></v-divider>
    <v-alert
      v-if="invitedPlayers.length === 0"
      dense
      text
      type="info"
      class="mt-3"
    >
      {{ $t("gameRoom.pregame.invitedList.noPlayers") }}
    </v-alert>
    <v-list v-else rounded>
      <v-list-item :key="index" v-for="(player, index) in invitedPlayers">
        <v-list-item-content>
          {{ player.username }}
        </v-list-item-content>
      </v-list-item>
    </v-list>
    <v-dialog
      v-if="gameHostId === this.$store.getters['user/getUser'].userId"
      v-model="dialog"
      width="500"
      persistent
    >
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          v-bind="attrs"
          v-on="on"
          color="primary"
          block
          tile
          class="rounded-b-sm"
          @click="loadInvitablePlayers"
        >
          {{ $t("gameRoom.pregame.invitedList.invite") }}
        </v-btn>
      </template>

      <v-card>
        <v-card-title>
          {{ $t("gameRoom.pregame.invitedList.invite") }}
        </v-card-title>

        <v-card-text>
          <v-select
            v-model="invitedPlayerList"
            :items="invitablePlayers"
            :menu-props="{ maxHeight: '400' }"
            multiple
            label="Select"
          ></v-select>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" text @click="dialog = false">
            {{ $t("gameLobby.createGame.cancel") }}
          </v-btn>
          <v-btn
            color="primary"
            text
            @click="invitePlayers"
            :disabled="invitedPlayerList === null"
          >
            {{ $t("gameRoom.pregame.invitedList.invite") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-sheet>
</template>

<script>
import { createInvite } from "@/services/invites.service";
import { getActiveUsers } from "@/services/user.service";

export default {
  name: "InvitedList",
  props: {
    gameHostId: Number,
    gameRoomId: Number,
    invitedPlayers: Array,
    gameRoomUsers: Array,
  },
  data() {
    return {
      dialog: false,
      invitablePlayers: [],
      invitedPlayerList: null,
      allPlayers: [],
    };
  },
  methods: {
    loadInvitablePlayers() {
      this.invitablePlayers = [];
      getActiveUsers()
        .then((response) => {
          this.allPlayers = response;
        })
        .then(() => {
          this.allPlayers
            .filter(
              (allPlayer) =>
                !this.gameRoomUsers
                  .map((gameRoomPlayer) => gameRoomPlayer.user.userId)
                  .includes(allPlayer.userId)
            )
            .filter(
              (allPlayer) =>
                !this.invitedPlayers
                  .map((invitedPlayer) => invitedPlayer.userId)
                  .includes(allPlayer.userId)
            )
            .forEach((player) => {
              this.invitablePlayers.push({
                text: player.username,
                value: player.userId,
              });
            });
        });
    },
    invitePlayers() {
      if (
        this.invitedPlayers.find(
          (player) => player.userId === this.invitedPlayer
        ) ||
        this.gameRoomUsers.find(
          (player) => player.user.userId === this.invitedPlayer
        )
      ) {
        this.$notify({
          title: this.$t("notification.error"),
          text: this.$t("notification.alreadyInvited"),
          type: "error",
        });
      } else {
        createInvite(this.gameRoomId, this.invitedPlayerList);
        this.$notify({
          title: this.$t("notification.success"),
          text: this.$t("notification.inviteSuccess"),
          type: "success",
        });
        this.invitedPlayerList = null;
      }
      this.dialog = false;
    },
  },
};
</script>
