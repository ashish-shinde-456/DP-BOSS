import SectionHeading from '../common/SectionHeading.jsx'
// Keep this import for when the Final Ank panel is restored.
// import { finalAnk } from '../../data/results.js'

function LuckyNumbersSection({ goldenAnk }) {
  const digits = goldenAnk.split('-')

  return (
    <section className="content-section" aria-labelledby="lucky-numbers-title">
      <SectionHeading id="lucky-numbers-title" title="Today Lucky Number" />
      <div className="number-grid golden-ank-only-grid">
        <article className="number-panel golden-ank-panel">
          <h3>Golden Ank</h3>
          <div className="golden-ank-coins" role="img" aria-label={`Golden Ank ${goldenAnk}`}>
            {digits.map((digit, index) => (
              <span className="golden-ank-coin" key={`${index}-${digit}`}>
                <span className="golden-ank-coin-value">{digit}</span>
              </span>
            ))}
          </div>
        </article>

        {/*
        <article className="number-panel">
          <h3>Final Ank</h3>
          <ul className="final-ank-list">
            {finalAnk.map((item) => (
              <li key={item.market}>
                <span>{item.market}</span>
                <strong>{item.value}</strong>
              </li>
            ))}
          </ul>
        </article>
        */}
      </div>
    </section>
  )
}

export default LuckyNumbersSection