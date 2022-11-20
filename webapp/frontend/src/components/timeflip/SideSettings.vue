<template>
  <v-row>
    <v-col
      ><span class="text-h6">{{
        $t("timeFlip.settings.side") + " " + side
      }}</span></v-col
    >
    <v-col>
      <v-select
        v-model="configuration.activity"
        :items="actions"
        dense
        :label="$t('timeFlip.settings.action')"
        :rules="rules"
      ></v-select>
    </v-col>
    <v-col>
      <v-select
        v-model="configuration.time"
        :items="time"
        dense
        :label="$t('timeFlip.settings.minutes')"
        :rules="rules"
      ></v-select>
    </v-col>
    <v-col>
      <v-select
        v-model="configuration.roundPoints"
        :items="points"
        dense
        :label="$t('timeFlip.settings.points')"
        :rules="rules"
      ></v-select>
    </v-col>
  </v-row>
</template>

<script>
export default {
  name: "SideSettings",
  props: {
    side: String,
    configuration: {
      type: Object,
      default: function () {
        return { time: 1, roundPoints: 1, activity: "DRAW" };
      },
    },
  },
  data() {
    return {
      actions: ["DRAW", "RHYME", "PANTOMIME", "SPEAK"],
      points: ["ONE", "TWO", "THREE"],
      time: ["ONE", "TWO", "THREE"],
      rules: [(value) => !!value || this.$t("timeFlip.settings.emptyForm")],
    };
  },
};
</script>

<style scoped></style>
