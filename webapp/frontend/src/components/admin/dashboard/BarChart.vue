<script>
import { HorizontalBar, mixins } from "vue-chartjs";
const { reactiveProp } = mixins;

export default {
  extends: HorizontalBar,
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
            yAxes: [
              {
                gridLines: {
                  display: false,
                },
              },
            ],
            xAxes: [
              {
                gridLines: {
                  display: true,
                },
              },
            ],
          },
          tooltips: {
            enabled: true,
            mode: "single",
          },
          legend: false,
        };
      },
    },
    plugin: Object,
  },
  mounted() {
    if (this.plugin) {
      this.addPlugin(this.plugin);
    }
    this.renderChart(this.chartData, this.options);
  },
};
</script>
