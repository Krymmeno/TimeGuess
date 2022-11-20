<template>
  <v-row>
    <v-col cols="12" v-show="games.length === 0">
      <v-alert text type="info" class="align-content-center">
        {{ $t("gameLobby.gameInvites.noGames") }}
      </v-alert>
    </v-col>
    <v-col cols="12" sm="6" md="3" :key="game.id" v-for="game in games">
      <GameCard :game="game" />
    </v-col>
  </v-row>
</template>

<script>
import GameCard from "@/components/game/gamelobby/GameCard.vue";
export default {
  name: "GameCards",
  components: { GameCard },
  props: {
    games: Array,
    stats: Object,
  },
};
</script>

<style>
.card-outter {
  padding-bottom: 50px;
}
.card-actions {
  position: absolute;
  bottom: 0;
  width: 100%;
}
</style>
