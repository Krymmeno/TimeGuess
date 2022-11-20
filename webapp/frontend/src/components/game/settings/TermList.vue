<template>
  <v-container>
    <v-row align="center">
      <v-col>
        <v-text-field
          v-model="search"
          append-icon="mdi-magnify"
          label="Search"
          single-line
          hide-details
        ></v-text-field>
      </v-col>
      <v-spacer />
      <v-col cols="auto">
        <CreateTerm @addTerm="addTerm" />
      </v-col>
      <v-col cols="auto">
        <UploadTopic @importTerms="importTerms" />
      </v-col>
      <v-col cols="auto">
        <v-btn fab small color="green" @click="$emit('downloadTerms')">
          <v-icon>mdi-download</v-icon>
        </v-btn>
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="12">
        <v-data-table
          :headers="headers"
          :items="termList"
          sort-by="id"
          :search="search"
        >
          <template v-slot:item.name="props">
            <v-edit-dialog
              large
              :return-value.sync="props.item.name"
              @open="backupTerm(props.item)"
              @save="$emit('termChanged', props.item, backup)"
            >
              {{ props.item.name }}
              <template v-slot:input>
                <v-text-field
                  v-model="props.item.name"
                  :rules="[min1char]"
                  label="Edit"
                  single-line
                  counter
                ></v-text-field>
              </template>
            </v-edit-dialog>
          </template>
          <template v-slot:item.delete="{ item }">
            <DeleteButton
              :item="item"
              @deleteItem="$emit('deleteTerm', item)"
              :message="
                $t('gameSettings.termList.deleteTerm') + ' ' + item.name + '?'
              "
            />
          </template>
        </v-data-table>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import DeleteButton from "@/components/general/DeleteButton";
import CreateTerm from "@/components/game/settings/CreateTerm";
import UploadTopic from "@/components/game/settings/UploadTopic";
export default {
  name: "TermList",
  components: { CreateTerm, DeleteButton, UploadTopic },
  props: {
    termList: Array,
  },
  methods: {
    addTerm({ term, onSuccess }) {
      this.$emit("addTerm", { term, onSuccess });
    },
    backupTerm(term) {
      this.backup = JSON.parse(JSON.stringify(term));
    },
    importTerms(terms) {
      this.$emit("importTerms", terms);
    },
  },
  data() {
    return {
      search: "",
      backup: null,
      headers: [
        {
          text: this.$t("gameSettings.termList.termId"),
          align: "start",
          value: "termId",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("gameSettings.termList.termName"),
          value: "name",
          sortable: false,
          class: "font-weight-bold subtitle-1",
        },
        { text: "", value: "delete", sortable: false, align: "end" },
      ],
      min1char: (v) => !!v || this.$t("gameSettings.termList.emptyForm"),
    };
  },
};
</script>

<style scoped></style>
