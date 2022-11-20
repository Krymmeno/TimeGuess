<template>
  <v-sheet rounded elevation="2">
    <h3 class="heading text-center py-2">
      {{ $t("gameRoom.pregame.waitingList.title") }}
    </h3>
    <v-divider></v-divider>
    <v-alert
      v-if="players.filter((player) => player.teamColor === null).length === 0"
      dense
      text
      type="info"
      class="mt-3"
    >
      {{ $t("gameRoom.pregame.waitingList.noPlayers") }}
    </v-alert>
    <v-list v-else rounded>
      <v-list-item
        :key="index"
        v-for="(player, index) in players.filter(
          (player) => player.teamColor === null
        )"
      >
        <v-list-item-icon>
          <v-icon v-if="player.user.userId === gameHostId"> mdi-star </v-icon>
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
  </v-sheet>
</template>

<script>
import { leaveGameRoom } from "@/services/gameroom.service";

export default {
  name: "WaitingList",
  props: {
    gameRoomId: Number,
    gameHostId: Number,
    players: Array,
  },
  methods: {
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
