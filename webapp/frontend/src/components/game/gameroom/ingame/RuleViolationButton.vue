<template>
  <v-dialog v-model="violationDialog" persistent max-width="500px">
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        block
        v-bind="attrs"
        v-on="on"
        color="purple"
        class="white--text"
        :disabled="roundStart === null || roundEnd !== null"
      >
        <v-icon left> mdi-alert-octagon-outline </v-icon>
        {{ $t("game.termSheet.ruleViolation.title") }}
      </v-btn>
    </template>
    <v-card>
      <v-card-title>
        <span class="headline">{{
          $t("game.termSheet.ruleViolation.title")
        }}</span>
      </v-card-title>
      <v-divider class="mb-5"></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="error" text @click="violationDialog = false">
          {{ $t("game.termSheet.ruleViolation.cancel") }}
        </v-btn>
        <v-btn color="purple" text @click="ruleViolation">
          {{ $t("game.termSheet.ruleViolation.punish") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: "RuleViolationButton",
  data() {
    return {
      violationDialog: false,
    };
  },
  props: {
    roundStart: String,
    roundEnd: String,
  },
  methods: {
    ruleViolation() {
      this.violationDialog = false;
      this.$emit("ruleViolation");
    },
  },
};
</script>
