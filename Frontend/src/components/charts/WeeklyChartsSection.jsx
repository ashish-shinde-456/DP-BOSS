import SectionHeading from '../common/SectionHeading.jsx'
import chartTables from '../../data/reference-tables.json'
import weeklyCharts from '../../data/weekly-charts.js'

function WeeklyChartsSection() {
  return (
    <section className="content-section" id="charts" aria-labelledby="weekly-charts-title">
      <SectionHeading id="weekly-charts-title" title="Starline and Market Charts" />
      {chartTables.slice(0, 3).map((table, tableIndex) => (
        <div className="chart-table-block" key={`${table.title}-${tableIndex}`}>
          <h3>{table.title}</h3>
          <div className="chart-placeholder">
            <table>
              <tbody>
                {table.rows.map((row, rowIndex) => (
                  <tr key={`${tableIndex}-${rowIndex}`}>
                    {row.map((cell, cellIndex) => rowIndex === 0
                      ? <th scope="col" key={`${cell}-${cellIndex}`}>{cell || '-'}</th>
                      : <td key={`${cell}-${cellIndex}`}>{cell || '-'}</td>)}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}
      {weeklyCharts.map((chart) => (
        <div className="weekly-chart-block" key={chart.title}>
          <h3>{chart.title}</h3>
          <ul>
            {chart.rows.map((row) => <li key={row}>{row}</li>)}
          </ul>
        </div>
      ))}
    </section>
  )
}

export default WeeklyChartsSection