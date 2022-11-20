<script>
import { Pie, mixins } from "vue-chartjs";
const { reactiveProp } = mixins;

export default {
  extends: Pie,
  mixins: [reactiveProp],
  props: {
    chartData: Object,
    options: {
      type: Object,
      default: () => {
        return {
          borderWidth: "10px",
          hoverBackgroundColor: "red",
          hoverBorderWidth: "10px",
          responsive: true,
          maintainAspectRatio: false,
        };
      },
    },
  },
  mounted() {
    this.renderChart(this.chartData, this.options);
  },
};
</script>
