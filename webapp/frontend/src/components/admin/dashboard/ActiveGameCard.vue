<template>
  <v-col cols="12" md="4">
    <v-sheet elevation="2" rounded>
      <h3 class="heading pa-2 primary rounded-t white--text">
        {{ $t("dashboard.activeGames.title") }}
      </h3>
      <v-alert
        dense
        text
        type="info"
        class="mt-3"
        v-if="activeGames.length === 0"
      >
        {{ $t("dashboard.activeGames.noGames") }}
      </v-alert>
      <div
        class="pa-3 pt-4"
        :key="index"
        v-for="(activeGame, index) in activeGames"
      >
        <div class="d-flex justify-space-between">
          <span class="body-1 align-self-center">{{ activeGame.name }}</span>
          <v-btn
            class="mb-2 amber"
            small
            :to="`/gamerooms/${activeGame.gameRoomId}`"
            >{{ $t("dashboard.activeGames.spectate") }}</v-btn
          >
        </div>
        <v-divider></v-divider>
      </div>
    </v-sheet>
  </v-col>
</template>

<script>
export default {
  name: "ActiveGameCard",
  props: {
    activeGames: Array,
  },
};
</script>
