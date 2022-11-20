<template>
  <v-sheet elevation="2" rounded class="pa-5">
    <h2 class="display-1">
      {{ $t("game.termSheet.topic") }}:
      <v-chip color="primary" class="text-uppercase">
        {{ game ? game.topic.name : "-" }}
      </v-chip>
    </h2>
    <h3 class="subtitle-1">
      {{ $t("game.termSheet.activity") }}:
      <span class="overline">{{
        game ? game.currentRound.activity : "-"
      }}</span>
    </h3>
    <h3 class="subtitle-1">
      {{ $t("game.termSheet.time") }}:
      <span class="overline">
        {{ game ? roundTime : 0 }}
        {{
          roundTime === 1
            ? $t("game.termSheet.minute")
            : $t("game.termSheet.minutes")
        }}
      </span>
    </h3>
    <h3 class="subtitle-1">
      {{ $t("game.termSheet.points") }}:
      <span class="overline">{{
        game ? game.currentRound.roundPoints : "-"
      }}</span>
    </h3>
    <h3 class="subtitle-1">
      {{ $t("game.termSheet.currentTeam") }}:
      <span class="overline">{{ game ? game.currentTeam.color : "-" }}</span>
    </h3>
    <h3 class="subtitle-1">
      {{ $t("game.termSheet.currentPlayer") }}:
      <span class="overline">{{
        game ? game.currentRound.user.username : "-"
      }}</span>
    </h3>

    <h1 class="text-h3 text-sm-h2 text-xl-h1 text-center mt-16">
      {{
        game
          ? game.currentTeam.players.find(
              (player) =>
                player.userId === this.$store.getters["user/getUser"].userId
            )
            ? "???"
            : game.currentRound.term.name.toUpperCase()
          : ""
      }}
    </h1>
    <v-progress-linear
      class="my-2"
      color="primary"
      rounded
      :value="(roundTimer / maxTime) * 100"
    ></v-progress-linear>
    <h3 class="text-center">
      {{ $t("game.termSheet.timeRemaining") }}:
      {{ String(Math.floor(roundTimer / 60)).padStart(2, "0") }}:{{
        String(roundTimer % 60).padStart(2, "0")
      }}
    </h3>
    <GameControls
      v-if="
        game
          ? !game.currentTeam.players.find(
              (player) =>
                player.userId === this.$store.getters['user/getUser'].userId
            ) && !spectator
          : false
      "
      :round-start="game ? game.currentRound.guessStart : null"
      :round-end="game ? game.currentRound.guessEnd : null"
      :round-result="game ? game.currentRound.result : null"
      @startRound="$emit('startRound')"
      @winRound="$emit('winRound')"
      @ruleViolation="$emit('ruleViolation')"
    />
  </v-sheet>
</template>

<script>
import GameControls from "@/components/game/gameroom/ingame/GameControls";

export default {
  name: "TermSheet",
  components: { GameControls },
  props: {
    game: Object,
    roundTime: Number,
    roundTimer: Number,
    maxTime: Number,
    spectator: Boolean,
  },
};
</script>
