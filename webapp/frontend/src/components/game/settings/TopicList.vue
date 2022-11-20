<template>
  <v-container>
    <v-row>
      <v-col cols="5">
        <v-text-field
          v-model="search"
          class="mb-4"
          append-icon="mdi-magnify"
          label="Search"
          single-line
          outlined
          hide-details
          dense
        ></v-text-field>
      </v-col>
    </v-row>
    <v-data-table
      :headers="headers"
      :items="topicList"
      sort-by="name"
      :search="search"
    >
      <template v-slot:item.amountTerms="{ item }">
        {{ item.terms.length }}
      </template>
      <template v-slot:item.modify="{ item }">
        <TopicDialog :topic="item" @dialogClosed="dialogClosed" />
      </template>
      <template v-slot:item.delete="{ item }">
        <DeleteButton
          :item="item"
          :message="
            $t('gameSettings.topicList.deleteTopic') + ' ' + item.name + '?'
          "
          @deleteItem="deleteTopic"
        />
      </template>
    </v-data-table>
  </v-container>
</template>

<script>
import DeleteButton from "@/components/general/DeleteButton";
import TopicDialog from "@/components/game/settings/TopicDialog";
export default {
  name: "UserList",
  components: { TopicDialog, DeleteButton },
  props: {
    topicList: Array,
  },
  data() {
    return {
      search: "",
      min1char: (v) => !!v || this.$t("login.emptyForm"),
      headers: [
        {
          text: this.$t("gameSettings.topicList.Id"),
          align: "start",
          value: "topicId",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("gameSettings.topicList.topicName"),
          value: "name",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("gameSettings.topicList.amountTerms"),
          value: "amountTerms",
          sortable: false,
          class: "font-weight-bold subtitle-1",
        },
        { text: "", value: "modify", sortable: false, align: "end" },
        { text: "", value: "delete", sortable: false },
      ],
    };
  },
  methods: {
    dialogClosed() {
      this.$emit("dialogClosed");
    },
    deleteTopic(topic) {
      this.$emit("deleteTopic", topic);
    },
  },
};
</script>

<style scoped></style>
