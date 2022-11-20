<template>
  <v-row class="mt-5">
    <v-col cols="12" sm="6">
      <v-btn
        block
        color="green"
        @click="$emit('startRound')"
        :disabled="roundResult === null"
      >
        <v-icon left> mdi-page-next-outline </v-icon>
        {{ $t("game.termSheet.nextRound") }}
      </v-btn>
    </v-col>
    <v-col cols="12" sm="6">
      <RuleViolationButton
        :round-start="roundStart"
        :round-end="roundEnd"
        @ruleViolation="$emit('ruleViolation')"
      />
    </v-col>
  </v-row>
</template>

<script>
import RuleViolationButton from "@/components/game/gameroom/ingame/RuleViolationButton";
export default {
  name: "GameControls",
  components: { RuleViolationButton },
  props: {
    roundStart: String,
    roundEnd: String,
    roundResult: String,
  },
};
</script>
