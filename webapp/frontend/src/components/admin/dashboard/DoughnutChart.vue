<script>
import { Doughnut, mixins } from "vue-chartjs";
import Chart from "chart.js";
const { reactiveProp } = mixins;

export default {
  extends: Doughnut,
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
          legend: {
            labels: {
              generateLabels: function (chart) {
                let labels = Chart.defaults.global.legend.labels.generateLabels(
                  chart
                );
                return labels;
              },
            },
          },
          tooltips: {
            callbacks: {
              label: function (tooltipItem, data) {
                var dataset = data.datasets[tooltipItem.datasetIndex];
                var index = tooltipItem.index;
                return dataset.labels[index] + ": " + dataset.data[index];
              },
            },
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
