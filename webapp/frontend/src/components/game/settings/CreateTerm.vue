<template>
  <v-dialog v-model="dialog" max-width="300px">
    <template v-slot:activator="{ on, attrs }">
      <v-btn small fab color="green" elevation="2" v-bind="attrs" v-on="on">
        <v-icon>mdi-plus</v-icon>
      </v-btn>
    </template>
    <v-card>
      <v-card-title>
        {{ $t("gameSettings.termList.newTerm") }}
      </v-card-title>
      <v-form
        v-model="valid"
        @submit.prevent="
          $emit('addTerm', { term: name, onSuccess: () => (dialog = false) })
        "
      >
        <v-card-text>
          <v-text-field v-model="name" :rules="[formNotEmpty]" />
        </v-card-text>
        <v-card-actions>
          <v-btn type="submit" :disabled="!valid" color="primary">
            {{ $t("gameSettings.termList.addTerm") }}
          </v-btn>
          <v-btn @click="dialog = false" text>
            {{ $t("gameSettings.termList.cancel") }}
          </v-btn>
        </v-card-actions>
      </v-form>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: "CreateTerm",
  data() {
    return {
      valid: false,
      name: "",
      dialog: false,
      formNotEmpty: (v) => !!v || this.$t("gameSettings.termList.emptyForm"),
    };
  },
};
</script>

<style scoped></style>
