<template>
  <v-card hover height="100%" class="card-outter">
    <v-card-title>
      {{ game.name }}
    </v-card-title>
    <v-card-text>
      <span class="font-weight-bold"
        >{{ $t("gameLobby.gameCard.topic") }}:</span
      >
      {{ game.topic.name }} <br />
      <span class="font-weight-bold"
        >{{ $t("gameLobby.gameCard.players") }}:</span
      >
      {{ game.gameRoomUsers.length }} <br />
      <span class="font-weight-bold">TimeFlip:</span>
      <v-chip
        small
        v-if="game.timeFlip !== null"
        class="ml-2"
        color="green"
        text-color="white"
      >
        {{ $t("gameLobby.gameCard.timeFlip.available") }}
      </v-chip>
      <v-chip small v-else class="ml-2" color="orange" text-color="white">
        {{ $t("gameLobby.gameCard.timeFlip.unavailable") }}
      </v-chip>
    </v-card-text>
    <v-card-actions class="card-actions">
      <v-btn color="primary" block @click="acceptInviteClick">
        {{ $t("gameLobby.gameCard.join") }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
import { acceptInvite } from "@/services/invites.service";

export default {
  name: "GameCard",
  props: {
    game: Object,
  },
  methods: {
    acceptInviteClick() {
      acceptInvite(this.game.gameRoomId).then(() => {
        this.$router.push(`/gamerooms/${this.game.gameRoomId}`);
      });
    },
  },
};
</script>
