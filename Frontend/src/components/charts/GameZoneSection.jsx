import spinnerImage from '../../assets/reference-spinner.png'
import SectionHeading from '../common/SectionHeading.jsx'
import chartTables from '../../data/reference-tables.json'

function GameZoneSection() {
  return (
    <section className="content-section game-zone-section" aria-labelledby="game-zone-title">
      <SectionHeading id="game-zone-title" title="Dpboss Special Game Zone" />
      <img className="game-spinner" src={spinnerImage} alt="Number wheel" />
      {chartTables.slice(3).map((table, tableIndex) => (
        <div className="chart-table-block" key={`${table.title}-${tableIndex}`}>
          <h3>{table.title}</h3>
          <div className="chart-placeholder">
            <table>
              <tbody>
                {table.rows.map((row, rowIndex) => (
                  <tr key={`${tableIndex}-${rowIndex}`}>
                    {row.map((cell, cellIndex) => (
                      <td key={`${tableIndex}-${rowIndex}-${cellIndex}`}>{cell || '-'}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}
    </section>
  )
}

export default GameZoneSection