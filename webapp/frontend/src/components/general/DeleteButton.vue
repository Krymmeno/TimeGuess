<template>
  <div>
    <v-btn
      small
      icon
      color="red"
      class="mr-2"
      @click="
        dialogDelete = true;
        userToDelete = item;
      "
      :disabled="disabled"
    >
      <v-icon>mdi-delete</v-icon>
    </v-btn>
    <v-dialog v-model="dialogDelete" width="unset">
      <v-card>
        <v-card-title class="headline">{{ message }} </v-card-title>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue darken-1" text @click="dialogDelete = false"
            >{{ $t("deleteButton.cancel") }}
          </v-btn>
          <v-btn
            color="blue darken-1"
            text
            @click="
              $emit('deleteItem', item);
              dialogDelete = false;
            "
            >OK
          </v-btn>
          <v-spacer></v-spacer>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
export default {
  name: "DeleteButton",
  props: {
    item: Object,
    message: String,
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      dialogDelete: false,
    };
  },
};
</script>

<style scoped></style>
