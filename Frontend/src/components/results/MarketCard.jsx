function MarketCard({
  name,
  result,
  resultDate,
  jodiHref,
  panelHref,
  openTime,
  closeTime,
  highlighted,
}) {
  return (
    <article className={`market-card${highlighted ? ' market-card-highlighted' : ''}`}>
      {jodiHref && <a className="market-action" href={jodiHref} aria-label={`${name} Jodi chart`}>Jodi</a>}
      <div className="market-card-content">
        <h3>{name}</h3>
        <p className="market-result">{result}</p>
        {resultDate && <p className="market-times">{resultDate}</p>}
        {(openTime || closeTime) && (
          <p className="market-times">
            <span>{openTime}</span>
            <span>{closeTime}</span>
          </p>
        )}
      </div>
      {panelHref && <a className="market-action" href={panelHref} aria-label={`${name} Panel chart`}>Panel</a>}
    </article>
  )
}

export default MarketCard