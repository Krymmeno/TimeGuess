<script>
import { Line, mixins } from "vue-chartjs";
const { reactiveProp } = mixins;

export default {
  extends: Line,
  mixins: [reactiveProp],
  props: {
    chartData: Object,
    options: {
      type: Object,
      default: () => {
        return {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            xAxes: [
              {
                gridLines: {
                  display: false,
                  zeroLineColor: "transparent",
                },
              },
            ],
          },
          tooltips: {
            mode: "nearest",
            intersect: false,
          },
          plugins: {
            legend: false,
          },
        };
      },
    },
  },
  mounted() {
    this.renderChart(this.chartData, this.options);
  },
};
</script>
