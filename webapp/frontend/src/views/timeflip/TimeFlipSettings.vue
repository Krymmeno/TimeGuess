<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <v-row>
          <v-col cols="9">
            <h1
              class="display-3 mt-16 mb-5"
              v-html="$t('timeFlip.settings.title')"
            />
          </v-col>
          <v-spacer />
        </v-row>
        <v-divider class="mb-5"></v-divider>
        <TimeFlipList
          :timeFlips="timeFlips"
          @dialogClosed="dialogClosed"
          @refresh="getTimeFlips"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import TimeFlipList from "@/components/timeflip/TimeFlipList";
import { getTimeFlips } from "@/services/timeflip.service";
export default {
  name: "TimeFlipSettings",
  components: { TimeFlipList },
  data() {
    return {
      timeFlips: [],
    };
  },
  methods: {
    getTimeFlips() {
      getTimeFlips().then((timeFlips) => {
        this.timeFlips = timeFlips;
      });
    },
    dialogClosed() {
      this.getTimeFlips();
    },
  },
  mounted() {
    this.getTimeFlips();
  },
};
</script>

<style scoped></style>
